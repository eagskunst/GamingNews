package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test

class ArticleReaderRemoteDataSourceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val okHttpClient: OkHttpClient = mockk()
    private val dataSource = ArticleReaderRemoteDataSource(
        okHttpClient = okHttpClient,
        dispatchers = TestDispatcherProvider()
    )

    private val url = "https://example.com/article"

    private fun buildResponse(code: Int, body: String): Response {
        val request = Request.Builder().url(url).build()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("message")
            .body(body.toResponseBody("text/html".toMediaType()))
            .build()
    }

    private fun mockResponse(response: Response) {
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } returns response
    }

    @Test
    fun `given successful response with non blank body when fetchHtml then returns body`() = runTest {
        mockResponse(buildResponse(200, "<html>content</html>"))

        val result = dataSource.fetchHtml(url)

        assertEquals("<html>content</html>", result)
    }

    @Test
    fun `given successful response with blank body when fetchHtml then returns null`() = runTest {
        mockResponse(buildResponse(200, "   "))

        val result = dataSource.fetchHtml(url)

        assertNull(result)
    }

    @Test
    fun `given unsuccessful response when fetchHtml then returns null`() = runTest {
        mockResponse(buildResponse(404, "not found"))

        val result = dataSource.fetchHtml(url)

        assertNull(result)
    }

    @Test
    fun `given exception during execute when fetchHtml then propagates exception`() = runTest {
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } throws RuntimeException("network failure")

        try {
            dataSource.fetchHtml(url)
            fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("network failure") == true)
        }
    }
}
