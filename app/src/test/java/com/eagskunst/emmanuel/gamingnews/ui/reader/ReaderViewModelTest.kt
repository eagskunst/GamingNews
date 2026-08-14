package com.eagskunst.emmanuel.gamingnews.ui.reader

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReaderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    private val okHttpClient: OkHttpClient = mockk()
    private val articleUrl = "https://example.com/article"

    private fun createViewModel(): ReaderViewModel = ReaderViewModel(
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository),
        okHttpClient = okHttpClient,
        dispatchers = TestDispatcherProvider(),
        savedStateHandle = SavedStateHandle(mapOf(ReaderActivity.EXTRA_URL to articleUrl))
    )

    private fun buildResponse(code: Int, body: String): Response {
        val request = Request.Builder().url(articleUrl).build()
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

    private fun mockException(exception: Exception) {
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } throws exception
    }

    @Test
    fun `given successful response with non blank body when initialized then uiState is Content`() = runTest {
        mockResponse(buildResponse(200, "<html>hello</html>"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Content)
            state as ReaderUiState.Content
            assertEquals(articleUrl, state.url)
            assertEquals("<html>hello</html>", state.html)
        }
    }

    @Test
    fun `given unsuccessful response when initialized then uiState is Error`() = runTest {
        mockResponse(buildResponse(404, "not found"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Error)
        }
    }

    @Test
    fun `given a successful response with a blank body when initialized then uiState is Error`() = runTest {
        mockResponse(buildResponse(200, ""))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Error)
        }
    }

    @Test
    fun `given an exception during execute when initialized then uiState is Error`() = runTest {
        mockException(RuntimeException("network failure"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Error)
        }
    }

    @Test
    fun `given a prior error when retry is called then uiState fetches again`() = runTest {
        mockException(RuntimeException("first failure"))
        val viewModel = createViewModel()

        mockResponse(buildResponse(200, "<html>retried</html>"))
        viewModel.retry()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Content)
            state as ReaderUiState.Content
            assertEquals("<html>retried</html>", state.html)
        }
    }

    @Test
    fun `given preferences when collected then darkThemeEnabled reflects darkTheme`() = runTest {
        mockResponse(buildResponse(200, "<html>hello</html>"))
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(darkTheme = true)

        val viewModel = createViewModel()

        viewModel.darkThemeEnabled.test {
            val darkTheme = expectMostRecentItem()
            assertTrue(darkTheme)
        }
    }
}
