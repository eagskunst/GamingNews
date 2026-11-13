package com.eagskunst.emmanuel.gamingnews.ui.gamedetail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRating
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GameDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val today = 20_000L

    private fun string(id: Int, vararg args: Any) = composeTestRule.activity.getString(id, *args)

    private fun setContent(
        state: GameDetailUiState,
        onRetry: () -> Unit = {},
        onFollowClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            GameDetailContent(
                uiState = state.copy(isLoading = false, todayEpochDay = today),
                onBack = {},
                onShare = { _, _ -> },
                onOpenGame = {},
                onRetry = onRetry,
                onFollowClick = onFollowClick
            )
        }
    }

    @Test
    fun `given an unreleased game then the countdown and hype tiles replace the scores`() {
        setContent(
            GameDetailUiState(
                details = Fixtures.gameDetails(
                    hypes = 1151,
                    platformReleases = listOf(Fixtures.platformRelease(epochDay = today + 7))
                )
            )
        )

        composeTestRule.onNodeWithText("7 days").assertIsDisplayed()
        composeTestRule.onNodeWithText("people hyped").assertIsDisplayed()
    }

    @Test
    fun `given a rated game then its score is shown`() {
        setContent(GameDetailUiState(details = Fixtures.gameDetails(userRating = GameRating(94, 5526))))

        composeTestRule.onNodeWithText("94").assertIsDisplayed()
    }

    @Test
    fun `given a platform without a date when platforms are expanded then it reads unknown date`() {
        setContent(
            GameDetailUiState(
                details = Fixtures.gameDetails(
                    platformReleases = listOf(
                        Fixtures.platformRelease(platformName = "PC"),
                        Fixtures.platformRelease(platformId = 508, platformName = "Switch 2", epochDay = null)
                    )
                )
            )
        )
        composeTestRule.onNodeWithText("PC, Switch 2").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("game_detail_platforms_toggle").performScrollTo().performClick()

        composeTestRule.onNodeWithText(string(R.string.game_detail_unknown_date)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `given images are disabled when the media tab opens then the setting is explained`() {
        setContent(
            GameDetailUiState(
                details = Fixtures.gameDetails(name = "Fable", screenshots = listOf("https://example.com/s.jpg")),
                loadImages = false
            )
        )

        composeTestRule.onNodeWithText(string(R.string.game_detail_tab_media)).performClick()

        composeTestRule.onNodeWithText(string(R.string.game_detail_images_disabled, "Fable")).assertIsDisplayed()
    }

    @Test
    fun `given expansions and dlc when the related tab opens then dlc stays behind its toggle`() {
        setContent(
            GameDetailUiState(
                details = Fixtures.gameDetails(expansions = listOf("Blood and Wine"), dlcs = listOf("Fool's Gold"))
            )
        )

        composeTestRule.onNodeWithText(string(R.string.game_detail_tab_related)).performClick()
        composeTestRule.onNodeWithText("Blood and Wine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fool's Gold").assertDoesNotExist()

        composeTestRule.onNodeWithText("Show 1 DLC pack").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Fool's Gold").assertIsDisplayed()
    }

    @Test
    fun `given a pending release when follow is tapped then the click is reported`() {
        var clicks = 0
        setContent(
            GameDetailUiState(details = Fixtures.gameDetails(), pendingRelease = Fixtures.gameRelease()),
            onFollowClick = { clicks++ }
        )

        composeTestRule.onNodeWithTag("game_detail_follow").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `given a released game then there is no follow button`() {
        setContent(GameDetailUiState(details = Fixtures.gameDetails(), pendingRelease = null))

        composeTestRule.onNodeWithTag("game_detail_follow").assertDoesNotExist()
    }

    @Test
    fun `given a failed load then retry is offered`() {
        var retries = 0
        setContent(GameDetailUiState(hasError = true), onRetry = { retries++ })

        composeTestRule.onNodeWithText(string(R.string.retry)).performClick()

        assertEquals(1, retries)
    }
}
