package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun newsStream(urls: List<String>): Flow<Result<List<NewsArticle>>>
    fun savedArticlesStream(): Flow<List<NewsArticle>>
    suspend fun saveArticle(article: NewsArticle)
    suspend fun removeArticle(article: NewsArticle)
    suspend fun isArticleSaved(article: NewsArticle): Boolean
}
