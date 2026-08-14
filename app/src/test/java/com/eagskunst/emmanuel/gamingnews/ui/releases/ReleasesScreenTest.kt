package com.eagskunst.emmanuel.gamingnews.ui.releases

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleasesRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReleasesScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeReleasesRepository = FakeReleasesRepository()

    private fun createViewModel(): ReleasesViewModel = ReleasesViewModel(fakeReleasesRepository)

    private fun setContent(viewModel: ReleasesViewModel = createViewModel()) {
        composeTestRule.setContent {
            ReleasesScreen(
                viewModel = viewModel,
                onSettingsClick = {},
                onOpenGameUrl = {}
            )
        }
    }

    @Test
    fun `given releases are loading when screen is shown then loading indicator is displayed`() {
        fakeReleasesRepository.releasesResultFlow.value = Result.Loading
        setContent()

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun `given a successful result when screen is shown then release names are rendered`() {
        val release = Fixtures.gameRelease(
            id = 10L,
            name = "Hollow Knight: Silksong",
            coverUrl = null
        )
        fakeReleasesRepository.releasesResultFlow.value = Result.Success(listOf(release))
        setContent()

        composeTestRule.onNodeWithText("Hollow Knight: Silksong").assertIsDisplayed()
    }

    @Test
    fun `given an error result when screen is shown then error message is displayed`() {
        fakeReleasesRepository.releasesResultFlow.value = Result.Error(RuntimeException("Failed to load releases"))
        setContent()

        composeTestRule.onNodeWithText("Failed to load releases").assertIsDisplayed()
    }
}
