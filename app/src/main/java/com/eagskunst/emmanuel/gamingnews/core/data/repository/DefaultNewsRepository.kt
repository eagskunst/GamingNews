package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toNewsArticle
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ArticleDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.RssRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.NewsRepository
import com.prof.rssparser.Channel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class DefaultNewsRepository @Inject constructor(
    private val rssRemoteDataSource: RssRemoteDataSource,
    private val articleDao: ArticleDao,
    private val dispatchers: DispatcherProvider
) : NewsRepository {

    override fun newsStream(urls: List<String>): Flow<Result<List<NewsArticle>>> = flow {
        emit(Result.Loading)
        val channels = fetchChannels(urls)
        val articles = channels.flatMap { channel ->
            val sourceName = channel.title ?: ""
            channel.articles.map { it.toNewsArticle(sourceName) }
        }.sortedByDescending { it.publicationDate }
        emit(Result.Success(articles))
    }.catch { e ->
        emit(Result.Error(e))
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

    private suspend fun fetchChannels(urls: List<String>): List<Channel> = supervisorScope {
        urls.map { url ->
            async {
                try {
                    rssRemoteDataSource.fetchChannel(url)
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }
}
