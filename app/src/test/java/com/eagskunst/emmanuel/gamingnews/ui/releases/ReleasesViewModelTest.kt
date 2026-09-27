package com.eagskunst.emmanuel.gamingnews.ui.releases

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelectionNotice
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformStatus
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UpcomingReleaseWindow
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.IgdbTokenAcquisitionException
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReleasesUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleasesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ReleasesViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private var catalog: PlatformCatalog = FakePlatformCatalog()

    /** Inside the upcoming-release window the use case applies by default. */
    private fun upcomingDate() = Date(System.currentTimeMillis() + 86_400_000L)

    private fun windowDate() = Date(UpcomingReleaseWindow.current().startMillis + 3_600_000L)

    /** Drains resumes queued on the unconfined main dispatcher (e.g. StateFlow re-collects). */
    private fun pumpMain() = mainDispatcher.scheduler.advanceUntilIdle()

    private fun viewModel(
        releases: FakeReleasesRepository = FakeReleasesRepository(),
        prefs: FakeUserPreferencesRepository = FakeUserPreferencesRepository(catalog = catalog)
    ): ReleasesViewModel = ReleasesViewModel(
        getReleases = GetReleasesUseCase(releases, catalog),
        releasesRepository = releases,
        userPreferencesRepository = prefs,
        catalog = catalog
    )

    @Test
    fun `given releases when initialized then grouped results and count come from the same result`() = runTest {
        val day = upcomingDate()
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 100, platformId = 6, releaseDate = day, name = "Shared Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 100, platformId = 48, releaseDate = day, name = "Shared Game")
        )
        val releases = FakeReleasesRepository(Result.Success(records))

        val viewModel = viewModel(releases)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(1, state.releases.size)
            assertEquals(1, state.matchCount)
            assertEquals(listOf("PC", "PS4"), state.releases.single().platforms)
            assertEquals(catalog.supportedPlatforms(), state.platformOptions)
        }
    }

    @Test
    fun `given a persisted platform selection when initialized then chips and results reflect it`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6")
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, releaseDate = upcomingDate(), name = "PC Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 48, releaseDate = upcomingDate(), name = "PS4 Game")
        )
        val releases = FakeReleasesRepository(Result.Success(records))

        val viewModel = viewModel(releases, prefs)

        val state = viewModel.uiState.value
        assertEquals(setOf(6), state.selectedPlatformIds)
        assertEquals(listOf("PC Game"), state.releases.map { it.name })
    }

    @Test
    fun `given a selection when a chip is toggled then the new set is persisted`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        val viewModel = viewModel(prefs = prefs)
        pumpMain()

        viewModel.onPlatformToggle(48)
        pumpMain()

        assertEquals(setOf("48"), prefs.storedPlatformIds.value)
        assertEquals(setOf(48), viewModel.uiState.value.selectedPlatformIds)
    }

    @Test
    fun `given the last selected platform when it is deselected then the selection falls back to All`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("48")
        val viewModel = viewModel(prefs = prefs)
        pumpMain()

        viewModel.onPlatformToggle(48)
        pumpMain()

        assertEquals(emptySet<String>(), prefs.storedPlatformIds.value)
        assertEquals(emptySet<Int>(), viewModel.uiState.value.selectedPlatformIds)
    }

    @Test
    fun `given filters when All is selected then only the platform selection is cleared`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6", "48")
        val viewModel = viewModel(prefs = prefs)
        pumpMain()
        viewModel.onSearchQueryChange("zelda")

        viewModel.onSelectAllPlatforms()
        pumpMain()

        assertEquals(emptySet<String>(), prefs.storedPlatformIds.value)
        assertEquals("zelda", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `given a failing write when a chip is toggled then the failure surfaces as an error`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.failPlatformWrites = true
        val viewModel = viewModel(prefs = prefs)
        pumpMain()

        viewModel.onPlatformToggle(48)
        pumpMain()

        assertEquals(ReleasesError.SELECTION_SAVE, viewModel.uiState.value.error)
    }

    @Test
    fun `given a retired stored platform when the selection loads then a catalog notice is raised and All shown`() = runTest {
        catalog = FakePlatformCatalog(
            listOf(
                FakePlatformCatalog.entry(igdbId = 6, name = "PC", order = 0),
                FakePlatformCatalog.entry(igdbId = 48, name = "PS4", order = 1, status = PlatformStatus.RETIRED)
            )
        )
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("48")

        val viewModel = viewModel(prefs = prefs)
        pumpMain()

        val state = viewModel.uiState.value
        assertEquals(PlatformSelectionNotice.ALL_RETIRED_FALLBACK, state.catalogNotice)
        assertEquals(emptySet<Int>(), state.selectedPlatformIds)
        assertEquals(emptySet<String>(), prefs.storedPlatformIds.value)
    }

    @Test
    fun `given a stored unknown platform when the selection loads then a removable fallback is kept`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("9999")

        val viewModel = viewModel(prefs = prefs)
        pumpMain()

        val state = viewModel.uiState.value
        assertEquals(setOf(9999), state.unknownPlatformIds)
        assertEquals(setOf(9999), state.selectedPlatformIds)
        assertNull(state.catalogNotice)

        viewModel.onPlatformToggle(9999)
        pumpMain()
        assertEquals(emptySet<String>(), prefs.storedPlatformIds.value)
    }

    @Test
    fun `given a search query when it changes then results are filtered`() = runTest {
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, releaseDate = upcomingDate(), name = "Mario Party"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 6, releaseDate = upcomingDate(), name = "Zelda Echoes")
        )
        val releases = FakeReleasesRepository(Result.Success(records))
        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.onSearchQueryChange("zelda")

        val state = viewModel.uiState.value
        assertEquals("zelda", state.searchQuery)
        assertEquals(listOf("Zelda Echoes"), state.releases.map { it.name })
        assertEquals(1, state.matchCount)
    }

    @Test
    fun `given zero matches and remaining pages when results settle then pages keep loading until exhausted`() = runTest {
        val releases = FakeReleasesRepository(Result.Success(emptyList()), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(
            listOf(Result.Success(true), Result.Success(true), Result.Success(false))
        )

        viewModel(releases)
        pumpMain()

        assertEquals(3, releases.loadNextPageInvocations)
    }

    @Test
    fun `given a pagination failure when it happens then loading stops and retry resumes`() = runTest {
        val releases = FakeReleasesRepository(Result.Success(emptyList()), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(
            listOf(Result.Error(RuntimeException("page failed")), Result.Success(false))
        )

        val viewModel = viewModel(releases)
        pumpMain()

        assertEquals(1, releases.loadNextPageInvocations)
        assertEquals(ReleasesError.PAGINATION, viewModel.uiState.value.error)

        viewModel.retry()
        pumpMain()

        assertEquals(2, releases.loadNextPageInvocations)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `given a refresh failure when retry is invoked then refresh is attempted again`() = runTest {
        val releases = FakeReleasesRepository()
        releases.refreshResult = Result.Error(RuntimeException("refresh failed"))
        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.refresh()
        pumpMain()
        assertEquals(ReleasesError.REFRESH, viewModel.uiState.value.error)

        releases.refreshResult = Result.Success(Unit)
        viewModel.retry()
        pumpMain()

        assertEquals(2, releases.refreshInvocations)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `given pagination in progress when loadMore is called again then it does not duplicate the request`() = runTest {
        val releases = FakeReleasesRepository(Result.Success(emptyList()), initialHasMorePages = true)
        val pendingLoad = CompletableDeferred<Unit>()
        releases.loadNextPageBlocker = pendingLoad
        releases.loadNextPageResult = Result.Success(false)

        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.loadMore()
        pumpMain()

        assertEquals(1, releases.loadNextPageInvocations)
        pendingLoad.complete(Unit)
    }

    @Test
    fun `given no more pages when loadMore is called then it is a no op`() = runTest {
        val releases = FakeReleasesRepository(initialHasMorePages = false)
        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.loadMore()

        assertEquals(0, releases.loadNextPageInvocations)
    }

    @Test
    fun `given the hasMorePages stream when it emits then the state reflects it`() = runTest {
        // Six or more groups fill the viewport threshold so the auto-loader doesn't consume
        // the hasMorePages = true emission and flip it back.
        val records = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = upcomingDate(),
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(records), initialHasMorePages = false)
        val viewModel = viewModel(releases)
        pumpMain()

        assertEquals(false, viewModel.uiState.value.hasMorePages)

        releases.hasMorePagesFlow.value = true
        pumpMain()

        assertTrue(viewModel.uiState.value.hasMorePages)
    }

    @Test
    fun `GIVEN a near end demand WHEN filtered pages do not match THEN pages drain until a match loads`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6")
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Match $index"
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
                        releaseDate = day, name = "Match PS4"
                    )
                ),
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 101, gameId = 101, platformId = 6,
                        releaseDate = day, name = "Other game"
                    )
                ),
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 102, gameId = 102, platformId = 6,
                        releaseDate = day, name = "Match later"
                    )
                )
            )
        )
        val viewModel = viewModel(releases, prefs)

        viewModel.onSearchQueryChange("Match")
        pumpMain()
        assertEquals(0, releases.loadNextPageInvocations)

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(3, releases.loadNextPageInvocations)
        assertTrue(viewModel.uiState.value.releases.any { it.name == "Match later" })
        assertEquals(false, viewModel.uiState.value.hasMorePages)
    }

    @Test
    fun `GIVEN a platform filter and near end WHEN pages have no platform match THEN paging continues to a match`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6")
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(listOf(Result.Success(true), Result.Success(false)))
        releases.loadNextPageRecords.addAll(
            listOf(
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 100, gameId = 100, platformId = 48,
                        releaseDate = day, name = "PS4 exclusive"
                    )
                ),
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 101, gameId = 101, platformId = 6,
                        releaseDate = day, name = "PC exclusive"
                    )
                )
            )
        )
        val viewModel = viewModel(releases, prefs)
        pumpMain()

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(2, releases.loadNextPageInvocations)
        assertTrue(viewModel.uiState.value.releases.any { it.name == "PC exclusive" })
        assertTrue(viewModel.uiState.value.releases.none { it.name == "PS4 exclusive" })
    }

    @Test
    fun `GIVEN duplicate page records WHEN near the end THEN no new cards appear and paging continues`() = runTest {
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(listOf(Result.Success(true), Result.Success(false)))
        releases.loadNextPageRecords.addAll(
            listOf(
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 100, gameId = 0, platformId = 48,
                        releaseDate = day, name = "Game 0"
                    )
                ),
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 101, gameId = 101, platformId = 6,
                        releaseDate = day, name = "Game new"
                    )
                )
            )
        )
        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(2, releases.loadNextPageInvocations)
        assertEquals(7, viewModel.uiState.value.releases.size)
        assertTrue(viewModel.uiState.value.releases.any { it.name == "Game new" })
    }

    @Test
    fun `GIVEN a page error WHEN near the end THEN paging stops until retry drains to exhaustion`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6")
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(
            listOf(
                Result.Success(true),
                Result.Error(RuntimeException("page failed")),
                Result.Success(false)
            )
        )
        releases.loadNextPageRecords.addAll(
            listOf(
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 100, gameId = 100, platformId = 48,
                        releaseDate = day, name = "PS4 only"
                    )
                ),
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 101, gameId = 101, platformId = 6,
                        releaseDate = day, name = "Game recovered"
                    )
                )
            )
        )
        val viewModel = viewModel(releases, prefs)
        pumpMain()

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(2, releases.loadNextPageInvocations)
        assertEquals(ReleasesError.PAGINATION, viewModel.uiState.value.error)

        releases.releasesResultFlow.value = Result.Success(
            initial + Fixtures.gameReleaseRecord(
                releaseId = 103, gameId = 103, platformId = 48,
                releaseDate = day, name = "Another PS4 only"
            )
        )
        pumpMain()

        assertEquals(ReleasesError.PAGINATION, viewModel.uiState.value.error)
        assertEquals(2, releases.loadNextPageInvocations)

        viewModel.onListEndVisibilityChanged(false)
        pumpMain()
        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(2, releases.loadNextPageInvocations)
        assertEquals(ReleasesError.PAGINATION, viewModel.uiState.value.error)

        viewModel.retry()
        pumpMain()

        assertEquals(3, releases.loadNextPageInvocations)
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.releases.any { it.name == "Game recovered" })
        assertEquals(false, viewModel.uiState.value.hasMorePages)
    }

    @Test
    fun `GIVEN a token failure on a page WHEN near the end THEN the auth error is shown and retry resumes paging`() = runTest {
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(
            listOf(
                Result.Error(IgdbTokenAcquisitionException(RuntimeException("token failed"))),
                Result.Success(false)
            )
        )
        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(1, releases.loadNextPageInvocations)
        assertEquals(ReleasesError.TOKEN_ACQUISITION, viewModel.uiState.value.error)

        viewModel.onListEndVisibilityChanged(false)
        pumpMain()
        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(1, releases.loadNextPageInvocations)
        assertEquals(ReleasesError.TOKEN_ACQUISITION, viewModel.uiState.value.error)

        viewModel.retry()
        pumpMain()

        assertEquals(2, releases.loadNextPageInvocations)
        assertNull(viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.hasMorePages)
    }

    @Test
    fun `GIVEN a pagination error WHEN refresh is invoked THEN the failure clears and demand resumes paging`() = runTest {
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(
            listOf(
                Result.Error(RuntimeException("page failed")),
                Result.Success(false)
            )
        )
        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(1, releases.loadNextPageInvocations)
        assertEquals(ReleasesError.PAGINATION, viewModel.uiState.value.error)

        viewModel.refresh()
        pumpMain()

        assertEquals(1, releases.refreshInvocations)
        assertEquals(2, releases.loadNextPageInvocations)
        assertNull(viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.hasMorePages)
    }

    @Test
    fun `GIVEN a text only filter over many matches WHEN near the end THEN an unmatched page continues to a later match`() = runTest {
        val day = windowDate()
        val initial = List(7) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Quest $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(listOf(Result.Success(true), Result.Success(false)))
        releases.loadNextPageRecords.addAll(
            listOf(
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 100, gameId = 100, platformId = 6,
                        releaseDate = day, name = "Different game"
                    )
                ),
                listOf(
                    Fixtures.gameReleaseRecord(
                        releaseId = 101, gameId = 101, platformId = 6,
                        releaseDate = day, name = "Quest Final"
                    )
                )
            )
        )
        val viewModel = viewModel(releases)

        viewModel.onSearchQueryChange("Quest")
        pumpMain()
        assertEquals(0, releases.loadNextPageInvocations)

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()

        assertEquals(2, releases.loadNextPageInvocations)
        assertTrue(viewModel.uiState.value.releases.any { it.name == "Quest Final" })
        assertEquals(8, viewModel.uiState.value.releases.size)
    }

    @Test
    fun `GIVEN an in flight page WHEN demand repeats THEN the request stays single and cleared demand stops paging`() = runTest {
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        val pendingLoad = CompletableDeferred<Unit>()
        releases.loadNextPageBlocker = pendingLoad
        releases.loadNextPageResult = Result.Success(true)
        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()
        viewModel.onListEndVisibilityChanged(true)
        viewModel.loadMore()
        pumpMain()

        assertEquals(1, releases.loadNextPageInvocations)

        viewModel.onListEndVisibilityChanged(false)
        pendingLoad.complete(Unit)
        pumpMain()

        assertEquals(1, releases.loadNextPageInvocations)
    }

    @Test
    fun `GIVEN an in flight page WHEN filters change THEN the next page uses the latest criteria without restarting the stream`() = runTest {
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        val pendingLoad = CompletableDeferred<Unit>()
        releases.loadNextPageBlocker = pendingLoad
        releases.loadNextPageResults.add(Result.Success(false))
        releases.loadNextPageRecords.add(
            listOf(
                Fixtures.gameReleaseRecord(
                    releaseId = 100, gameId = 100, platformId = 6,
                    releaseDate = day, name = "Zelda II"
                )
            )
        )
        val viewModel = viewModel(releases)
        pumpMain()

        viewModel.onListEndVisibilityChanged(true)
        pumpMain()
        assertEquals(1, releases.loadNextPageInvocations)

        viewModel.onSearchQueryChange("zelda")
        pumpMain()

        assertEquals(1, releases.releasesStreamInvocations)
        assertEquals(1, releases.loadNextPageInvocations)
        assertEquals(0, viewModel.uiState.value.releases.size)

        pendingLoad.complete(Unit)
        pumpMain()

        assertEquals(1, releases.releasesStreamInvocations)
        assertEquals(listOf("Zelda II"), viewModel.uiState.value.releases.map { it.name })
    }

    @Test
    fun `GIVEN enough matches without near end demand WHEN loadMore is called THEN only one page is fetched`() = runTest {
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("6")
        val day = windowDate()
        val initial = List(6) { index ->
            Fixtures.gameReleaseRecord(
                releaseId = index.toLong(),
                gameId = index.toLong(),
                platformId = 6,
                releaseDate = day,
                name = "Game $index"
            )
        }
        val releases = FakeReleasesRepository(Result.Success(initial), initialHasMorePages = true)
        releases.loadNextPageResults.addAll(listOf(Result.Success(true), Result.Success(false)))
        releases.loadNextPageRecords.add(
            listOf(
                Fixtures.gameReleaseRecord(
                    releaseId = 100, gameId = 100, platformId = 48,
                    releaseDate = day, name = "PS4 exclusive"
                )
            )
        )
        val viewModel = viewModel(releases, prefs)
        pumpMain()

        viewModel.loadMore()
        pumpMain()

        assertEquals(1, releases.loadNextPageInvocations)
        assertEquals(true, viewModel.uiState.value.hasMorePages)
    }

    @Test
    fun `given catalog notices when dismissed then the notice is cleared`() = runTest {
        catalog = FakePlatformCatalog(
            listOf(
                FakePlatformCatalog.entry(igdbId = 6, name = "PC", order = 0),
                FakePlatformCatalog.entry(igdbId = 48, name = "PS4", order = 1, status = PlatformStatus.RETIRED)
            )
        )
        val prefs = FakeUserPreferencesRepository(catalog = catalog)
        prefs.storedPlatformIds.value = setOf("48")
        val viewModel = viewModel(prefs = prefs)
        pumpMain()

        viewModel.dismissCatalogNotice()

        assertNull(viewModel.uiState.value.catalogNotice)
    }
}
