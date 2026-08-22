package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

/**
 * Reusable [NewsRepository] fake backed by [MutableStateFlow]s, shared across all use
 * case/ViewModel/Compose UI tests that depend on news articles.
 */
class FakeNewsRepository(
    initialNewsResult: Result<List<NewsArticle>> = Result.Success(emptyList()),
    initialSavedArticles: List<NewsArticle> = emptyList()
) : NewsRepository {

    val newsResultFlow = MutableStateFlow(initialNewsResult)
    val savedArticlesFlow = MutableStateFlow(initialSavedArticles)

    var lastRequestedUrls: List<String>? = null
        private set
    var lastForceRefresh: Boolean? = null
        private set

    override fun newsStream(urls: List<String>, forceRefresh: Boolean): Flow<Result<List<NewsArticle>>> {
        lastRequestedUrls = urls
        lastForceRefresh = forceRefresh
        return flow { emit(newsResultFlow.value) }
    }


    override fun savedArticlesStream(): Flow<List<NewsArticle>> = savedArticlesFlow

    override suspend fun saveArticle(article: NewsArticle) {
        savedArticlesFlow.update { it + article }
    }

    override suspend fun removeArticle(article: NewsArticle) {
        savedArticlesFlow.update { it - article }
    }

    override suspend fun isArticleSaved(article: NewsArticle): Boolean =
        savedArticlesFlow.value.contains(article)
}
