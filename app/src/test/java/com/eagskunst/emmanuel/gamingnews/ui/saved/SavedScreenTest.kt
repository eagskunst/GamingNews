package com.eagskunst.emmanuel.gamingnews.ui.saved

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeNewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SavedScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeNewsRepository = FakeNewsRepository()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository(Fixtures.userPreferences(loadImages = false))

    private fun createViewModel(): SavedViewModel = SavedViewModel(
        getSavedArticlesUseCase = GetSavedArticlesUseCase(fakeNewsRepository),
        toggleSavedArticleUseCase = ToggleSavedArticleUseCase(fakeNewsRepository),
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository)
    )

    private fun setContent(viewModel: SavedViewModel = createViewModel()) {
        composeTestRule.setContent {
            SavedScreen(
                viewModel = viewModel,
                onSettingsClick = {},
                onOpenArticle = {},
                onOpenArticleWithMode = { _, _ -> },
                onShareArticle = {}
            )
        }
    }

    @Test
    fun `given empty saved articles when screen is rendered then the no saved articles message is shown`() {
        fakeNewsRepository.savedArticlesFlow.value = emptyList()
        setContent()

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.no_saved_articles))
            .assertIsDisplayed()
    }

    @Test
    fun `given saved articles when screen is rendered then the list is rendered`() {
        val article = Fixtures.newsArticle(
            title = "Saved Headline",
            link = "https://example.com/saved"
        )
        fakeNewsRepository.savedArticlesFlow.value = listOf(article)
        setContent()

        composeTestRule.onNodeWithText("Saved Headline").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Remove from saved").assertIsDisplayed()
    }

    @Test
    fun `when save is toggled on an article then it is removed from the list`() {
        val article = Fixtures.newsArticle(
            title = "Removable Headline",
            link = "https://example.com/removable"
        )
        fakeNewsRepository.savedArticlesFlow.value = listOf(article)
        setContent()

        composeTestRule.onNodeWithText("Removable Headline").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Remove from saved").performClick()
        composeTestRule.waitForIdle()

        assertTrue(fakeNewsRepository.savedArticlesFlow.value.isEmpty())
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.no_saved_articles))
            .assertIsDisplayed()
    }
}
