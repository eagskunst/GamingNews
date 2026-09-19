package com.eagskunst.emmanuel.gamingnews.ui.reader

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderElement
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReaderArticleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeArticleReaderRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReaderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    private val fakeArticleReaderRepository = FakeArticleReaderRepository()

    private val articleUrl = "https://example.com/article"

    private fun createViewModel(): ReaderViewModel = ReaderViewModel(
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository),
        getReaderArticleUseCase = GetReaderArticleUseCase(fakeArticleReaderRepository),
        savedStateHandle = SavedStateHandle(mapOf(ReaderActivity.EXTRA_URL to articleUrl))
    )

    @Test
    fun `given successful parse when initialized then uiState is Content`() = runTest {
        val article = ReaderArticleFixtures.simpleArticle()
        fakeArticleReaderRepository.articleFlow.value = article

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Content)
            state as ReaderUiState.Content
            assertEquals(article.title, state.article.title)
        }
    }

    @Test
    fun `given null article when initialized then uiState is Error`() = runTest {
        fakeArticleReaderRepository.articleFlow.value = null

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Error)
        }
    }

    @Test
    fun `given exception during fetch when initialized then uiState is Error`() = runTest {
        fakeArticleReaderRepository.shouldThrow = true

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Error)
        }
    }

    @Test
    fun `given a prior error when retry is called then uiState fetches again`() = runTest {
        fakeArticleReaderRepository.shouldThrow = true
        val viewModel = createViewModel()

        fakeArticleReaderRepository.shouldThrow = false
        val article = ReaderArticleFixtures.simpleArticle()
        fakeArticleReaderRepository.articleFlow.value = article
        viewModel.retry()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state is ReaderUiState.Content)
            state as ReaderUiState.Content
            assertEquals(article.title, state.article.title)
        }
    }

    @Test
    fun `given preferences when collected then darkThemeEnabled reflects dark theme`() = runTest {
        fakeArticleReaderRepository.articleFlow.value = ReaderArticleFixtures.simpleArticle()
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(darkTheme = true)

        val viewModel = createViewModel()

        viewModel.darkThemeEnabled.test {
            assertTrue(expectMostRecentItem())
        }
    }

    @Test
    fun `given preferences when collected then loadImages reflects user preference`() = runTest {
        fakeArticleReaderRepository.articleFlow.value = ReaderArticleFixtures.simpleArticle()
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(loadImages = false)

        val viewModel = createViewModel()

        viewModel.loadImages.test {
            assertFalse(expectMostRecentItem())
        }
    }

    private object ReaderArticleFixtures {
        fun simpleArticle() = com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle(
            title = "Test Article",
            byline = "By Author",
            siteName = "Example Site",
            elements = listOf(
                ReaderElement.Paragraph(html = "<p>Hello world</p>"),
                ReaderElement.Heading(level = 2, text = "Section")
            )
        )
    }
}
