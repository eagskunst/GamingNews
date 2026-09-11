package com.eagskunst.emmanuel.gamingnews.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReviewArticleCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun reviewArticle(title: String, imageUrl: String?) = Fixtures.newsArticle(
        title = title,
        sourceName = "Test Reviews",
        author = "Reviewer",
        imageUrl = imageUrl,
        publicationDate = Date()
    )

    @Test
    fun `given hero article when card is rendered then title and author are displayed`() {
        composeTestRule.setContent {
            ReviewArticleCard(
                article = reviewArticle("Hero Review", "https://example.com/hero.png"),
                isSaved = false,
                loadImages = true,
                isHero = true,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithText("Hero Review").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reviewer", substring = true).assertIsDisplayed()
    }

    @Test
    fun `given hero article when loadImages is false then no image is shown`() {
        composeTestRule.setContent {
            ReviewArticleCard(
                article = reviewArticle("Hero Review", "https://example.com/hero.png"),
                isSaved = false,
                loadImages = false,
                isHero = true,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithText("Hero Review").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Article image").assertDoesNotExist()
    }

    @Test
    fun `given article with unknown date when card is rendered then relative date is omitted`() {
        val article = reviewArticle("Undated Review", null).copy(publicationDate = Date(0))

        composeTestRule.setContent {
            ReviewArticleCard(
                article = article,
                isSaved = false,
                loadImages = true,
                isHero = false,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithText("Undated Review").assertIsDisplayed()
        composeTestRule.onNodeWithText("years ago", substring = true).assertDoesNotExist()
    }

    @Test
    fun `given saved article when card is rendered then remove content description is shown`() {
        composeTestRule.setContent {
            ReviewArticleCard(
                article = reviewArticle("Saved Review", null),
                isSaved = true,
                loadImages = true,
                isHero = false,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Remove from saved").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Save").assertDoesNotExist()
    }

    @Test
    fun `given card when save is toggled then callback is invoked`() {
        var toggled = false
        composeTestRule.setContent {
            ReviewArticleCard(
                article = reviewArticle("Review", null),
                isSaved = false,
                loadImages = true,
                isHero = false,
                onToggleSave = { toggled = true },
                onClick = {},
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Save").performClick()

        assertTrue(toggled)
    }

    @Test
    fun `given card when clicked then onClick callback is invoked`() {
        var clicked = false
        composeTestRule.setContent {
            ReviewArticleCard(
                article = reviewArticle("Review", null),
                isSaved = false,
                loadImages = true,
                isHero = false,
                onToggleSave = {},
                onClick = { clicked = true },
                onMenuAction = {}
            )
        }

        composeTestRule.onNodeWithText("Review").performClick()

        assertTrue(clicked)
    }
}
