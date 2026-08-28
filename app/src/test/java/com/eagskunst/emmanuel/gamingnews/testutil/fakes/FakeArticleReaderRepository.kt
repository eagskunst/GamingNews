package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ArticleReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeArticleReaderRepository : ArticleReaderRepository {

    val articleFlow = MutableStateFlow<ReaderArticle?>(null)
    var shouldThrow = false

    override suspend fun fetchArticle(url: String): ReaderArticle? {
        if (shouldThrow) throw RuntimeException("Reader failure")
        return articleFlow.value
    }
}
