package com.eagskunst.emmanuel.gamingnews.ui.reviews

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewFeedSnapshot
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReviewsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeNewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReviewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReviewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeReviewsRepository: FakeReviewsRepository
    private lateinit var fakeNewsRepository: FakeNewsRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository

    @Before
    fun setUp() {
        fakeReviewsRepository = FakeReviewsRepository()
        fakeNewsRepository = FakeNewsRepository()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): ReviewsViewModel =
        ReviewsViewModel(
            getReviewsUseCase = GetReviewsUseCase(fakeReviewsRepository),
            getSavedArticlesUseCase = GetSavedArticlesUseCase(fakeNewsRepository),
            toggleSavedArticleUseCase = ToggleSavedArticleUseCase(fakeNewsRepository),
            getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository),
            savedStateHandle = savedStateHandle
        )

    @Test
    fun `given saved articles when initialized then savedLinks reflects saved article links`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/a1")
        fakeNewsRepository.savedArticlesFlow.value = listOf(article)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(setOf("https://example.com/a1"), state.savedLinks)
        }
    }

    @Test
    fun `given preferences when initialized then loadImages is applied to state`() = runTest {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(loadImages = false)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(false, state.loadImages)
        }
    }

    @Test
    fun `given successful reviews result when initialized then articles are updated`() = runTest {
        val article = Fixtures.newsArticle()
        fakeReviewsRepository.reviewsResultFlow.value = Result.Success(ReviewFeedSnapshot(listOf(article)))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf(article), state.articles)
            assertEquals(false, state.isLoading)
        }
    }

    @Test
    fun `given error result when initialized then errorMessage is updated`() = runTest {
        fakeReviewsRepository.reviewsResultFlow.value = Result.Error(RuntimeException("boom"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(false, state.isLoading)
            assertEquals("boom", state.errorMessage)
        }
    }

    @Test
    fun `when refresh is called then repository receives forceRefresh true`() = runTest {
        val viewModel = createViewModel()

        viewModel.refresh(forceRefresh = true)

        assertTrue(fakeReviewsRepository.lastForceRefresh == true)
    }

    @Test
    fun `given stale source failure when success then staleSourceMessage is set`() = runTest {
        val article = Fixtures.newsArticle()
        fakeReviewsRepository.reviewsResultFlow.value = Result.Success(
            ReviewFeedSnapshot(
                articles = listOf(article),
                failedSources = listOf(
                    com.eagskunst.emmanuel.gamingnews.core.domain.model.FailedReviewSource(
                        id = "reviews-ign-en",
                        name = "IGN",
                        reason = com.eagskunst.emmanuel.gamingnews.core.domain.model.FailedReviewSource.FailureReason.UnsupportedEligibilityMetadata
                    )
                )
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf(article), state.articles)
            assertTrue(state.staleSourceMessage?.contains("IGN") == true)
        }
    }

    @Test
    fun `given article when toggleSavedArticle is called then use case is invoked`() = runTest {
        val article = Fixtures.newsArticle()
        val viewModel = createViewModel()

        viewModel.toggleSavedArticle(article)

        assertTrue(fakeNewsRepository.savedArticlesFlow.value.contains(article))
    }

    @Test
    fun `given save failure when toggleSavedArticle is called then error is exposed`() = runTest {
        val article = Fixtures.newsArticle()
        fakeNewsRepository.saveError = IllegalStateException("Could not save")
        val viewModel = createViewModel()

        viewModel.toggleSavedArticle(article)

        viewModel.uiState.test {
            assertEquals("Could not save", expectMostRecentItem().errorMessage)
        }
    }

    @Test
    fun `given a query when onSearchQueryChange is called then searchQuery is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("zelda")

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("zelda", state.searchQuery)
        }
    }

    @Test
    fun `given query in savedStateHandle when initialized then searchQuery is restored`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf("reviews_search_query" to "mario")))

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("mario", state.searchQuery)
        }
    }
}
