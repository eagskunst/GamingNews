package com.eagskunst.emmanuel.gamingnews.ui.releases

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformStatus
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UpcomingReleaseWindow
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReleasesUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleasesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class ReleasesScreenTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var catalog: PlatformCatalog = FakePlatformCatalog()

    private fun viewModel(
        releases: FakeReleasesRepository = FakeReleasesRepository(),
        prefs: FakeUserPreferencesRepository = FakeUserPreferencesRepository(catalog = catalog)
    ): ReleasesViewModel = ReleasesViewModel(
        getReleases = GetReleasesUseCase(releases, catalog),
        releasesRepository = releases,
        userPreferencesRepository = prefs,
        catalog = catalog
    )

    private fun setContent(viewModel: ReleasesViewModel) {
        composeTestRule.setContent {
            ReleasesScreen(
                viewModel = viewModel,
                onSettingsClick = {},
                onOpenGameUrl = {}
            )
        }
    }

    /** Drains resumes queued on the unconfined main dispatcher, then settles composition. */
    private fun settle() {
        mainDispatcher.scheduler.advanceUntilIdle()
        composeTestRule.waitForIdle()
    }

    private fun upcomingDate() = Date(System.currentTimeMillis() + 86_400_000L)

    private fun windowDate() = Date(UpcomingReleaseWindow.current().startMillis + 3_600_000L)

    @Test
    fun `given releases are loading when screen is shown then loading indicator is displayed`() {
        val releases = FakeReleasesRepository(Result.Loading)
        setContent(viewModel(releases))

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun `given a successful result when screen is shown then release names and total count are rendered`() {
        val release = Fixtures.gameReleaseRecord(
            releaseId = 10L,
            gameId = 10L,
            platformId = 6,
            releaseDate = upcomingDate(),
            name = "Hollow Knight: Silksong"
        )
        val releases = FakeReleasesRepository(Result.Success(listOf(release)), initialHasMorePages = false)
        setContent(viewModel(releases))
        settle()

        composeTestRule.onNodeWithText("Hollow Knight: Silksong").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 matching release").assertIsDisplayed()
    }

    @Test
    fun `given more pages remaining when screen is shown then the loaded count label is used`() {
        // Six groups fill the viewport threshold so the auto-loader does not start fetching,
        // and the fake keeps reporting more pages so the "loaded" count variant sticks.
        val records = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = upcomingDate(),
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(records), initialHasMorePages = true)
        val pendingLoad = CompletableDeferred<Unit>()
        releases.loadNextPageBlocker = pendingLoad
        setContent(viewModel(releases))
        settle()

        composeTestRule.onNodeWithText("6 matching releases loaded").assertIsDisplayed()

        releases.loadNextPageResult = Result.Success(false)
        pendingLoad.complete(Unit)
        settle()
    }

    @Test
    fun `GIVEN filtered releases WHEN scrolled to the end THEN unmatched pages drain until a match loads`() {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6")
        val day = windowDate()
        val initial = List(12) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Base Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(
            listOf(Result.Success(true), Result.Success(true), Result.Success(false))
        )
        releases.loadNextPageRecords.addAll(
            listOf(
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 100, gameId = 100, platformId = 48,
                        releaseDate = day, name = "PS4 Only 1"
                    )
                ),
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 101, gameId = 101, platformId = 48,
                        releaseDate = day, name = "PS4 Only 2"
                    )
                ),
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 102, gameId = 102, platformId = 6,
                        releaseDate = day, name = "Late PC Match"
                    )
                )
            )
        )
        setContent(viewModel(releases, prefs))
        settle()

        composeTestRule.onNodeWithTag("releases_list").performScrollToIndex(initial.size)
        settle()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            releases.loadNextPageInvocations >= 3
        }
        settle()

        composeTestRule.onNodeWithTag("releases_list").performScrollToIndex(initial.size + 1)
        settle()

        composeTestRule.onNodeWithText("Late PC Match").assertIsDisplayed()
        assertEquals(3, releases.loadNextPageInvocations)
    }

    @Test
    fun `GIVEN near list end WHEN a fetched page fills the viewport THEN paging stops`() {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6")
        val day = windowDate()
        val initial = List(12) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Base Game $index"
            )
        }
        val nextPage = List(20) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = (100 + index).toLong(),
                gameId = (100 + index).toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Page Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(listOf(Result.Success(true), Result.Success(true)))
        releases.loadNextPageRecords.add(nextPage)
        val secondPageBlocker = CompletableDeferred<Unit>()
        releases.loadNextPageBlockers.addAll(
            listOf(CompletableDeferred(Unit), secondPageBlocker)
        )
        setContent(viewModel(releases, prefs))
        settle()

        composeTestRule.onNodeWithTag("releases_list").performScrollToIndex(initial.size)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            releases.loadNextPageInvocations >= 2
        }
        settle()
        secondPageBlocker.complete(Unit)
        settle()

        assertEquals(2, releases.loadNextPageInvocations)
        assertEquals(true, releases.hasMorePages.value)
    }

    @Test
    fun `GIVEN a blocked page near the end WHEN the screen leaves composition THEN no further page is requested`() {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6")
        val day = windowDate()
        val initial = List(12) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Base Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        val pendingLoad = CompletableDeferred<Unit>()
        releases.loadNextPageBlocker = pendingLoad
        releases.loadNextPageResult = Result.Success(true)
        val showScreen = mutableStateOf(true)
        val vm = viewModel(releases, prefs)
        composeTestRule.setContent {
            if (showScreen.value) {
                ReleasesScreen(
                    viewModel = vm,
                    onSettingsClick = {},
                    onOpenGameUrl = {}
                )
            }
        }
        settle()

        composeTestRule.onNodeWithTag("releases_list").performScrollToIndex(initial.size)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            releases.loadNextPageInvocations >= 1
        }

        showScreen.value = false
        composeTestRule.waitForIdle()
        mainDispatcher.scheduler.advanceUntilIdle()
        pendingLoad.complete(Unit)
        settle()

        assertEquals(1, releases.loadNextPageInvocations)
    }

    @Test
    fun `given an error result when screen is shown then the error message and retry are displayed`() {
        val releases = FakeReleasesRepository(Result.Error(RuntimeException("Failed to load releases")))
        setContent(viewModel(releases))
        settle()

        composeTestRule
            .onNodeWithText("Couldn’t refresh releases. Showing saved releases when available.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `given the screen when shown then the All chip and catalog platform chips are displayed`() {
        setContent(viewModel())
        settle()

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("PS5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Switch 2").assertIsDisplayed()

        // The row is a LazyRow: the last catalog chip lives offscreen until scrolled to.
        composeTestRule.onNodeWithTag("platform_filter_row")
            .performScrollToIndex(catalog.supportedPlatforms().size)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Xbox One").assertIsDisplayed()
    }

    @Test
    fun `given platform records when a chip is selected then only matching releases are shown`() {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, releaseDate = upcomingDate(), name = "PC Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 167, releaseDate = upcomingDate(), name = "PS5 Game")
        )
        val releases = FakeReleasesRepository(Result.Success(records))
        setContent(viewModel(releases, prefs))
        settle()

        // The chip node carries Checkbox semantics; release cards only carry text.
        val chipRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)
        composeTestRule.onNode(chipRole and hasText("PS5")).performClick()
        settle()

        composeTestRule.onNodeWithText("PS5 Game").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("PC Game").assertCountEquals(0)

        composeTestRule.onNode(chipRole and hasText("All")).performClick()
        settle()

        composeTestRule.onNodeWithText("PC Game").assertIsDisplayed()
    }

    @Test
    fun `given no matches with an active filter when complete then empty state offers to clear filters`() {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("48")
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, releaseDate = upcomingDate(), name = "PC Game")
        )
        val releases = FakeReleasesRepository(Result.Success(records), initialHasMorePages = false)
        setContent(viewModel(releases, prefs))
        settle()

        composeTestRule.onNodeWithText("No releases match your filters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear filters").performClick()
        settle()

        composeTestRule.onNodeWithText("PC Game").assertIsDisplayed()
    }

    @Test
    fun `given a stored unknown platform when screen is shown then a removable fallback chip is displayed`() {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("9999")
        setContent(viewModel(prefs = prefs))
        settle()

        // The unknown-selection chip is appended after the catalog chips; scroll to it.
        composeTestRule.onNodeWithTag("platform_filter_row")
            .performScrollToIndex(catalog.supportedPlatforms().size + 1)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Platform 9999").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Some saved platforms aren’t recognized. Remove them from the filter if you don’t need them.")
            .assertIsDisplayed()
    }

    @Test
    fun `given a retired stored platform when screen is shown then the catalog change notice is displayed`() {
        catalog = FakePlatformCatalog(
            listOf(
                FakePlatformCatalog.entry(igdbId = 6, name = "PC", order = 0),
                FakePlatformCatalog.entry(igdbId = 48, name = "PS4", order = 1, status = PlatformStatus.RETIRED)
            )
        )
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("48")
        setContent(viewModel(prefs = prefs))
        settle()

        composeTestRule
            .onNodeWithText("The platforms you selected are no longer available. Showing all platforms.")
            .assertIsDisplayed()
    }
}
