package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ArticleDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Reusable in-memory [ArticleDao] fake, mirroring the real DAO's `savedAt DESC` ordering and
 * REPLACE-on-conflict insert semantics.
 */
class FakeArticleDao : ArticleDao {

    private val articlesFlow = MutableStateFlow<List<ArticleEntity>>(emptyList())

    override fun observeAll(): Flow<List<ArticleEntity>> = articlesFlow

    override suspend fun getByLink(link: String): ArticleEntity? =
        articlesFlow.value.find { it.link == link }

    override suspend fun insert(article: ArticleEntity) {
        articlesFlow.value = (articlesFlow.value.filter { it.link != article.link } + article)
            .sortedByDescending { it.savedAt }
    }

    override suspend fun delete(article: ArticleEntity) {
        articlesFlow.value = articlesFlow.value.filter { it.link != article.link }
    }
}
