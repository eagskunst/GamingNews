package com.eagskunst.emmanuel.gamingnews.ui.main

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.OpenArticleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UpdateArticleOpenModeUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    private val openArticleUseCase: OpenArticleUseCase = mockk(relaxed = true)

    private fun createViewModel(): MainActivityViewModel = MainActivityViewModel(
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository),
        openArticleUseCase = openArticleUseCase,
        updateArticleOpenModeUseCase = UpdateArticleOpenModeUseCase(fakeUserPreferencesRepository)
    )

    @Test
    fun `given preferences when collected then themeMode and dynamicColor reflect user preferences`() = runTest {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(
            themeMode = ThemeMode.DARK,
            dynamicColor = false
        )

        val viewModel = createViewModel()

        viewModel.themeMode.test {
            assertEquals(ThemeMode.DARK, expectMostRecentItem())
        }
        viewModel.dynamicColor.test {
            assertFalse(expectMostRecentItem())
        }
        viewModel.darkThemeEnabled.test {
            assertTrue(expectMostRecentItem())
        }
    }

    @Test
    fun `given preferences when collected then articleOpenMode reflects the current mode`() = runTest {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(articleOpenMode = ArticleOpenMode.READER_MODE)

        val viewModel = createViewModel()

        viewModel.articleOpenMode.test {
            assertEquals(ArticleOpenMode.READER_MODE, expectMostRecentItem())
        }
    }

    @Test
    fun `given url and mode when openArticle is called then use case is invoked with them`() = runTest {
        val viewModel = createViewModel()

        viewModel.openArticle("https://example.com/a", ArticleOpenMode.EXTERNAL_BROWSER)

        verify { openArticleUseCase.invoke("https://example.com/a", ArticleOpenMode.EXTERNAL_BROWSER) }
    }

    @Test
    fun `given url and mode when openArticleWithMode is called then preference is updated and use case is invoked`() = runTest {
        val viewModel = createViewModel()

        viewModel.openArticleWithMode("https://example.com/b", ArticleOpenMode.READER_MODE)

        assertEquals(ArticleOpenMode.READER_MODE, fakeUserPreferencesRepository.preferencesFlow.value.articleOpenMode)
        verify { openArticleUseCase.invoke("https://example.com/b", ArticleOpenMode.READER_MODE) }
    }
}
