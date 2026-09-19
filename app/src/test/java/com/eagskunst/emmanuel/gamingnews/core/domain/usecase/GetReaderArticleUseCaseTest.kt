package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderElement
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ArticleReaderRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetReaderArticleUseCaseTest {

    @Test
    fun `given repository returns article when invoked then returns article`() = runTest {
        val article = ReaderArticle(
            title = "Article",
            byline = null,
            siteName = null,
            elements = listOf(ReaderElement.Paragraph(html = "<p>body</p>"))
        )
        val useCase = GetReaderArticleUseCase(FakeRepository(article))

        val result = useCase("https://example.com/article")

        assertEquals(article, result)
    }

    @Test
    fun `given repository returns null when invoked then returns null`() = runTest {
        val useCase = GetReaderArticleUseCase(FakeRepository(null))

        val result = useCase("https://example.com/article")

        assertNull(result)
    }

    private class FakeRepository(private val value: ReaderArticle?) : ArticleReaderRepository {
        override suspend fun fetchArticle(url: String): ReaderArticle? = value
    }
}
