package com.eagskunst.emmanuel.gamingnews.ui.reviews

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewFeedSnapshot
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReviewsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeMuteRulesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeNewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReviewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReviewsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeReviewsRepository = FakeReviewsRepository()
    private val fakeNewsRepository = FakeNewsRepository()
    private val fakeMuteRulesRepository = FakeMuteRulesRepository()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createViewModel(): ReviewsViewModel = ReviewsViewModel(
        getReviewsUseCase = GetReviewsUseCase(
            fakeReviewsRepository,
            fakeMuteRulesRepository,
            fakeUserPreferencesRepository,
            TestDispatcherProvider()
        ),
        getSavedArticlesUseCase = GetSavedArticlesUseCase(fakeNewsRepository),
        toggleSavedArticleUseCase = ToggleSavedArticleUseCase(fakeNewsRepository),
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository),
        savedStateHandle = SavedStateHandle()
    )

    @Test
    fun `given articles when screen is rendered then title and first article are displayed`() {
        val article = Fixtures.newsArticle(title = "Review Title", sourceName = "Eurogamer ES", author = "Author")
        fakeReviewsRepository.reviewsResultFlow.value = Result.Success(ReviewFeedSnapshot(listOf(article)))
        fakeNewsRepository.savedArticlesFlow.value = emptyList()

        composeTestRule.setContent {
            ReviewsScreen(
                viewModel = createViewModel(),
                onSettingsClick = {},
                onManageMutedWords = {},
                onOpenArticle = {},
                onOpenArticleWithMode = { _, _ -> },
                onShareArticle = {}
            )
        }

        composeTestRule.onNodeWithText("Reviews").assertIsDisplayed()
        composeTestRule.onNodeWithText("Review Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Author", substring = true).assertIsDisplayed()
    }

    @Test
    fun `given no articles when screen is rendered then empty message is displayed`() {
        fakeReviewsRepository.reviewsResultFlow.value = Result.Success(ReviewFeedSnapshot(emptyList()))

        composeTestRule.setContent {
            ReviewsScreen(
                viewModel = createViewModel(),
                onSettingsClick = {},
                onManageMutedWords = {},
                onOpenArticle = {},
                onOpenArticleWithMode = { _, _ -> },
                onShareArticle = {}
            )
        }

        composeTestRule.onNodeWithText("No reviews available right now").assertIsDisplayed()
    }

    @Test
    fun `given save icon when clicked then toggleSavedArticle is invoked`() {
        val article = Fixtures.newsArticle(title = "Review Title")
        fakeReviewsRepository.reviewsResultFlow.value = Result.Success(ReviewFeedSnapshot(listOf(article)))
        fakeNewsRepository.savedArticlesFlow.value = emptyList()

        composeTestRule.setContent {
            ReviewsScreen(
                viewModel = createViewModel(),
                onSettingsClick = {},
                onManageMutedWords = {},
                onOpenArticle = {},
                onOpenArticleWithMode = { _, _ -> },
                onShareArticle = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Save").performClick()

        assertTrue(fakeNewsRepository.savedArticlesFlow.value.contains(article))
    }
}
