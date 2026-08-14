package com.eagskunst.emmanuel.gamingnews.ui.saved

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeNewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class SavedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeNewsRepository = FakeNewsRepository()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()

    private fun createViewModel(): SavedViewModel = SavedViewModel(
        getSavedArticlesUseCase = GetSavedArticlesUseCase(fakeNewsRepository),
        toggleSavedArticleUseCase = ToggleSavedArticleUseCase(fakeNewsRepository),
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository)
    )

    @Test
    fun `given saved articles when initialized then articles are collected into state`() = runTest {
        val article = Fixtures.newsArticle()
        fakeNewsRepository.savedArticlesFlow.value = listOf(article)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf(article), state.articles)
        }
    }

    @Test
    fun `given preferences when initialized then loadImages is collected into state`() = runTest {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(loadImages = false)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.loadImages)
        }
    }

    @Test
    fun `given article when toggleSavedArticle is called then use case is invoked`() = runTest {
        val article = Fixtures.newsArticle()
        fakeNewsRepository.savedArticlesFlow.value = listOf(article)
        val viewModel = createViewModel()

        viewModel.toggleSavedArticle(article)

        assertEquals(emptyList<NewsArticle>(), fakeNewsRepository.savedArticlesFlow.value)
    }

    @Test
    fun `given a query when onSearchQueryChange is called then state reflects the query`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("elden ring")

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("elden ring", state.searchQuery)
        }
    }
}
