package com.eagskunst.emmanuel.gamingnews.ui.news

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetFeedUrlsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetNewsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeNewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NewsScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeNewsRepository = FakeNewsRepository()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository(Fixtures.userPreferences(loadImages = false))

    private fun createViewModel(): NewsViewModel {
        val context = ApplicationProvider.getApplicationContext<Application>()
        return NewsViewModel(
            getNewsUseCase = GetNewsUseCase(fakeNewsRepository),
            getSavedArticlesUseCase = GetSavedArticlesUseCase(fakeNewsRepository),
            toggleSavedArticleUseCase = ToggleSavedArticleUseCase(fakeNewsRepository),
            getFeedUrlsUseCase = GetFeedUrlsUseCase(context),
            getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository)
        )
    }

    private fun setContent(viewModel: NewsViewModel = createViewModel()) {
        composeTestRule.setContent {
            NewsScreen(
                viewModel = viewModel,
                onSettingsClick = {},
                onOpenArticle = {},
                onOpenArticleWithMode = { _, _ -> },
                onShareArticle = {}
            )
        }
    }

    @Test
    fun `given loading state when screen is rendered then a progress indicator is shown`() {
        fakeNewsRepository.newsResultFlow.value = Result.Loading
        setContent()

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun `given success state when screen is rendered then article titles are displayed`() {
        val article = Fixtures.newsArticle(
            title = "Test Article Title",
            link = "https://example.com/article"
        )
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(article))
        setContent()

        composeTestRule.onNodeWithText("Test Article Title").assertIsDisplayed()
    }

    @Test
    fun `given error state when screen is rendered then the error message is shown`() {
        fakeNewsRepository.newsResultFlow.value = Result.Error(RuntimeException("Something went wrong"))
        setContent()

        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    }

    @Test
    fun `when save icon is clicked on an article then its saved state is toggled`() {
        val article = Fixtures.newsArticle(
            title = "Saveable Article",
            link = "https://example.com/saveable"
        )
        fakeNewsRepository.savedArticlesFlow.value = emptyList()
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(article))
        setContent()

        composeTestRule.onNodeWithContentDescription("Save article").performClick()
        composeTestRule.waitForIdle()
        assertTrue(fakeNewsRepository.savedArticlesFlow.value.contains(article))
        composeTestRule.onNodeWithContentDescription("Remove from saved").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Remove from saved").performClick()
        composeTestRule.waitForIdle()
        assertFalse(fakeNewsRepository.savedArticlesFlow.value.contains(article))
        composeTestRule.onNodeWithContentDescription("Save article").assertIsDisplayed()
    }

    @Test
    fun `given a new articles banner when it is tapped then it is dismissed`() {
        val existingArticle = Fixtures.newsArticle(
            title = "Existing Article",
            link = "https://example.com/existing"
        )
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(existingArticle))
        val viewModel = createViewModel()
        setContent(viewModel)
        composeTestRule.waitForIdle()

        val newArticle = Fixtures.newsArticle(
            title = "New Article",
            link = "https://example.com/new"
        )
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(existingArticle, newArticle))
        viewModel.refresh(forceRefresh = true, notifyNewArticles = true)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1 new article").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 new article").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1 new article").assertDoesNotExist()
    }
}
