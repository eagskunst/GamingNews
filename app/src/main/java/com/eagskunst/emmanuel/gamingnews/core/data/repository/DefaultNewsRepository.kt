package com.eagskunst.emmanuel.gamingnews.core.data.repository

import android.util.Log
import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toNewsArticle
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ArticleDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.RssRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.NewsRepository
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class DefaultNewsRepository @Inject constructor(
    private val rssRemoteDataSource: RssRemoteDataSource,
    private val articleDao: ArticleDao,
    private val dispatchers: DispatcherProvider
) : NewsRepository {

    private val feedCache = ConcurrentHashMap<String, List<NewsArticle>>()

    override fun newsStream(urls: List<String>, forceRefresh: Boolean): Flow<Result<List<NewsArticle>>> = flow {
        val key = cacheKey(urls)
        val cached = feedCache[key]

        if (!forceRefresh && !cached.isNullOrEmpty()) {
            emit(Result.Success(cached))
        }

        if (forceRefresh || cached == null) {
            emit(Result.Loading)
        }

        try {
            val articles = fetchAndMergeArticles(urls)
            feedCache[key] = articles
            emit(Result.Success(articles))
        } catch (e: Exception) {
            if (!cached.isNullOrEmpty()) {
                emit(Result.Success(cached))
            } else {
                emit(Result.Error(e))
            }
        }
    }.flowOn(dispatchers.io)


    override fun savedArticlesStream(): Flow<List<NewsArticle>> =
        articleDao.observeAll()
            .map { entities -> entities.map { it.toNewsArticle() } }
            .flowOn(dispatchers.io)

    override suspend fun saveArticle(article: NewsArticle) {
        articleDao.insert(article.toArticleEntity())
    }

    override suspend fun removeArticle(article: NewsArticle) {
        articleDao.delete(article.toArticleEntity())
    }

    override suspend fun isArticleSaved(article: NewsArticle): Boolean {
        return articleDao.getByLink(article.link) != null
    }

    private suspend fun fetchAndMergeArticles(urls: List<String>): List<NewsArticle> {
        val channels = fetchChannels(urls)
        return channels.flatMap { channel ->
            val sourceName = channel.title ?: ""
            channel.items.map { it.toNewsArticle(sourceName) }
        }.sortedByDescending { it.publicationDate }
    }

    private suspend fun fetchChannels(urls: List<String>): List<RssChannel> = supervisorScope {
        urls.map { url ->
            async {
                try {
                    val channel = rssRemoteDataSource.fetchChannel(url)
                    // Log.i("Channel response", "Channel articles: ${channel.items}")
                    channel
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private fun cacheKey(urls: List<String>): String = urls.sorted().joinToString(",")
}
