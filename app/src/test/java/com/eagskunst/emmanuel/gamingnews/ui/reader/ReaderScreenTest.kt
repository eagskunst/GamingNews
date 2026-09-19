package com.eagskunst.emmanuel.gamingnews.ui.reader

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.reader.ReaderElement
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReaderArticleUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeArticleReaderRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReaderScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeArticleReaderRepository = FakeArticleReaderRepository()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository(
        Fixtures.userPreferences(loadImages = true)
    )

    private fun createViewModel(): ReaderViewModel {
        return ReaderViewModel(
            getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository),
            getReaderArticleUseCase = GetReaderArticleUseCase(fakeArticleReaderRepository),
            savedStateHandle = androidx.lifecycle.SavedStateHandle(
                mapOf(ReaderActivity.EXTRA_URL to "https://example.com/article")
            )
        )
    }

    @Test
    fun `given article content when screen is rendered then title and paragraphs are displayed`() {
        fakeArticleReaderRepository.articleFlow.value = ReaderArticle(
            title = "Reader Article Title",
            byline = "By Author",
            siteName = null,
            elements = listOf(
                ReaderElement.Paragraph(html = "Paragraph one"),
                ReaderElement.Heading(level = 2, text = "Section"),
                ReaderElement.Paragraph(html = "Paragraph two")
            )
        )

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = createViewModel(),
                isDarkTheme = false,
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Reader Article Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("By Author").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paragraph one").assertIsDisplayed()
        composeTestRule.onNodeWithText("Section").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paragraph two").assertIsDisplayed()
    }

    @Test
    fun `given failed parse when screen is rendered then error message is displayed`() {
        fakeArticleReaderRepository.articleFlow.value = null

        composeTestRule.setContent {
            ReaderScreen(
                viewModel = createViewModel(),
                isDarkTheme = false,
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Open in browser").assertIsDisplayed()
    }
}
