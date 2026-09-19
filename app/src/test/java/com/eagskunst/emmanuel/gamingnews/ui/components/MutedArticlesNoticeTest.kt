package com.eagskunst.emmanuel.gamingnews.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MutedArticlesNoticeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `given hidden muted articles then the notice shows the count and actions`() {
        composeTestRule.setContent {
            MutedArticlesNotice(
                mutedCount = 3,
                revealed = false,
                onToggleReveal = {},
                onManage = {}
            )
        }

        composeTestRule.onNodeWithText("3 articles hidden").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show hidden").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manage").assertIsDisplayed()
    }

    @Test
    fun `given revealed muted articles then the notice shows the revealed copy`() {
        composeTestRule.setContent {
            MutedArticlesNotice(
                mutedCount = 2,
                revealed = true,
                onToggleReveal = {},
                onManage = {}
            )
        }

        composeTestRule.onNodeWithText("Showing 2 muted articles").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hide again").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manage").assertIsDisplayed()
    }

    @Test
    fun `given no muted articles then the notice renders nothing`() {
        composeTestRule.setContent {
            MutedArticlesNotice(
                mutedCount = 0,
                revealed = false,
                onToggleReveal = {},
                onManage = {}
            )
        }

        composeTestRule.onNodeWithText("Show hidden").assertDoesNotExist()
        composeTestRule.onNodeWithText("Manage").assertDoesNotExist()
    }

    @Test
    fun `when the actions are tapped then the callbacks fire`() {
        var revealed = false
        var managed = false
        composeTestRule.setContent {
            MutedArticlesNotice(
                mutedCount = 1,
                revealed = false,
                onToggleReveal = { revealed = true },
                onManage = { managed = true }
            )
        }

        composeTestRule.onNodeWithText("Show hidden").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Manage").performClick()
        composeTestRule.waitForIdle()

        assertTrue(revealed)
        assertTrue(managed)
    }

    @Test
    fun `given the all-muted empty state then it shows reveal and manage actions`() {
        var revealed = false
        composeTestRule.setContent {
            AllMutedEmptyState(
                onShowHidden = { revealed = true },
                onManage = {}
            )
        }

        composeTestRule.onNodeWithText("Everything here is muted").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show hidden").performClick()
        composeTestRule.waitForIdle()
        assertTrue(revealed)
    }

    @Test
    fun `given a revealed muted article then the muted badge is shown on the card`() {
        composeTestRule.setContent {
            ArticleCard(
                article = testArticle(),
                isSaved = false,
                loadImages = false,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {},
                isRevealedMuted = true
            )
        }

        composeTestRule.onNodeWithText("Muted").assertIsDisplayed()
    }

    @Test
    fun `given a normal article then the muted badge is not shown`() {
        composeTestRule.setContent {
            ArticleCard(
                article = testArticle(),
                isSaved = false,
                loadImages = false,
                onToggleSave = {},
                onClick = {},
                onMenuAction = {},
                isRevealedMuted = false
            )
        }

        composeTestRule.onNodeWithText("Muted").assertDoesNotExist()
    }

    private fun testArticle() = Fixtures.newsArticle(
        title = "Some article",
        imageUrl = null
    )
}
