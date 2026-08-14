package com.eagskunst.emmanuel.gamingnews.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArticleCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var article: NewsArticle

    @Before
    fun setUp() {
        article = Fixtures.newsArticle(
            title = "Article Card Title",
            sourceName = "Polygon",
            imageUrl = null
        )
    }

    @Test
    fun `given an article when the card is rendered then the title and source name are displayed`() {
        composeTestRule.setContent {
            ArticleCard(
                article = article,
                isSaved = false,
                loadImages = false,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithText("Article Card Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Polygon", substring = true).assertIsDisplayed()
    }

    @Test
    fun `when the card is clicked then onClick is invoked`() {
        var clicked = false
        composeTestRule.setContent {
            ArticleCard(
                article = article,
                isSaved = false,
                loadImages = false,
                onToggleSave = {},
                onClick = { clicked = true },
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithText("Article Card Title").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `when the save icon is clicked then onToggleSave is invoked`() {
        var toggled = false
        composeTestRule.setContent {
            ArticleCard(
                article = article,
                isSaved = false,
                loadImages = false,
                onToggleSave = { toggled = true },
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Save article").performClick()
        assertTrue(toggled)
    }

    @Test
    fun `when the card is long pressed and an action is selected then onMenuAction is invoked`() {
        var receivedAction: ArticleMenuAction? = null
        composeTestRule.setContent {
            ArticleCard(
                article = article,
                isSaved = false,
                loadImages = false,
                onToggleSave = {},
                onClick = {},
                onMenuAction = { receivedAction = it }
            )
        }

        composeTestRule.onNodeWithText("Article Card Title").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.article_share))
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals(ArticleMenuAction.SHARE, receivedAction)
    }

    @Test
    fun `given the article is not saved when the card is rendered then the favorite icon content description is Save article`() {
        composeTestRule.setContent {
            ArticleCard(
                article = article,
                isSaved = false,
                loadImages = false,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Save article").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Remove from saved").assertDoesNotExist()
    }

    @Test
    fun `given the article is saved when the card is rendered then the favorite icon content description is Remove from saved`() {
        composeTestRule.setContent {
            ArticleCard(
                article = article,
                isSaved = true,
                loadImages = false,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Remove from saved").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Save article").assertDoesNotExist()
    }
}
