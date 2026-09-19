package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.ArticleReaderRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DefaultArticleReaderRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val remoteDataSource: ArticleReaderRemoteDataSource = mockk()
    private val repository = DefaultArticleReaderRepository(
        remoteDataSource = remoteDataSource,
        dispatchers = TestDispatcherProvider()
    )

    private val url = "https://example.com/article"

    @Test
    fun `given valid article html when fetchArticle then returns parsed article`() = runTest {
        val html = sampleArticleHtml()
        coEvery { remoteDataSource.fetchHtml(url) } returns html

        val article = repository.fetchArticle(url)

        assertNotNull(article)
        assertEquals("Sample Article Title", article!!.title)
        assertTrue(article.elements.isNotEmpty())
        assertTrue(article.elements.any { it is com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderElement.Paragraph })
    }

    @Test
    fun `given null html when fetchArticle then returns null`() = runTest {
        coEvery { remoteDataSource.fetchHtml(url) } returns null

        val article = repository.fetchArticle(url)

        assertNull(article)
    }

    @Test
    fun `given very short content when fetchArticle then returns null`() = runTest {
        coEvery { remoteDataSource.fetchHtml(url) } returns "<html><body><p>Short</p></body></html>"

        val article = repository.fetchArticle(url)

        assertNull(article)
    }

    private fun sampleArticleHtml(): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Sample Article Title</title>
                <meta name="author" content="Jane Doe">
            </head>
            <body>
                <article>
                    <h1>Sample Article Title</h1>
                    <p>By Jane Doe</p>
                    <p>This is the first paragraph of the article. It contains enough text to be considered meaningful content by the readability parser.</p>
                    <p>This is the second paragraph. It also has a good amount of text so that the parser can identify the article body correctly.</p>
                    <figure>
                        <img src="https://example.com/image.png" alt="Sample image">
                        <figcaption>An example image</figcaption>
                    </figure>
                    <p>Yet another paragraph with plenty of words to make the content long enough for extraction.</p>
                </article>
            </body>
            </html>
        """.trimIndent()
    }
}
