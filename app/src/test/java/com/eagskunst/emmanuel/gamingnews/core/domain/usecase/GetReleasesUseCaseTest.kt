package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredReleases
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReleaseDateRange
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReleaseFilters
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class GetReleasesUseCaseTest {

    private val catalog = FakePlatformCatalog()
    private val fakeRepository = FakeReleasesRepository()
    private val useCase = GetReleasesUseCase(fakeRepository, catalog)

    private val unboundedRange = ReleaseDateRange(0L, Long.MAX_VALUE)

    private fun filters(
        platformIds: Set<Int> = emptySet(),
        searchQuery: String = "",
        dateRange: ReleaseDateRange = unboundedRange
    ) = ReleaseFilters(platformIds = platformIds, searchQuery = searchQuery, dateRange = dateRange)

    @Test
    fun `given a successful result when invoke is called then records are emitted as filtered groups`() = runTest {
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, name = "Game A"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 48, name = "Game B")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters())).first() as Result.Success

        assertEquals(2, result.data.releases.size)
        assertEquals(2, result.data.matchCount)
        assertEquals(1, fakeRepository.releasesStreamInvocations)
    }

    @Test
    fun `given same game on several platforms the same day when grouped then one card combines the platform labels`() = runTest {
        val day = Date(86_400_000L * 20_000L)
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 167, releaseDate = day, name = "Shared Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 10, platformId = 6, releaseDate = day, name = "Shared Game"),
            Fixtures.gameReleaseRecord(releaseId = 3, gameId = 10, platformId = 48, releaseDate = day, name = "Shared Game")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters())).first() as Result.Success

        assertEquals(1, result.data.releases.size)
        assertEquals(listOf("PS5", "PC", "PS4"), result.data.releases.single().platforms)
        assertEquals(1, result.data.matchCount)
    }

    @Test
    fun `given same game on different days when grouped then one card per game and calendar date`() = runTest {
        val dayOne = Date(86_400_000L * 20_000L)
        val dayTwo = Date(86_400_000L * 20_001L)
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, releaseDate = dayTwo, name = "Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 10, platformId = 48, releaseDate = dayOne, name = "Game")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters())).first() as Result.Success

        assertEquals(2, result.data.releases.size)
        assertEquals(dayOne, result.data.releases[0].releaseDate)
        assertEquals(dayTwo, result.data.releases[1].releaseDate)
    }

    @Test
    fun `given a platform selection when filtering then only matching platforms remain`() = runTest {
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, name = "PC Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 48, name = "PS4 Game")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters(platformIds = setOf(48)))).first() as Result.Success

        assertEquals(listOf("PS4 Game"), result.data.releases.map { it.name })
    }

    @Test
    fun `given a multi platform selection when filtering then OR matching applies across platforms`() = runTest {
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, name = "PC Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 48, name = "PS4 Game"),
            Fixtures.gameReleaseRecord(releaseId = 3, gameId = 30, platformId = 130, name = "Switch Game")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters(platformIds = setOf(6, 130)))).first() as Result.Success

        assertEquals(listOf("PC Game", "Switch Game"), result.data.releases.map { it.name }.sorted())
    }

    @Test
    fun `given a search query when filtering then case insensitive name matching intersects the platform filter`() = runTest {
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, name = "Zelda Echoes"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 6, name = "Mario Party"),
            Fixtures.gameReleaseRecord(releaseId = 3, gameId = 30, platformId = 48, name = "Zelda Remake")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(
            flowOf(filters(platformIds = setOf(6), searchQuery = "zelda"))
        ).first() as Result.Success

        assertEquals(listOf("Zelda Echoes"), result.data.releases.map { it.name })
    }

    @Test
    fun `given records on unsupported platforms when filtering then they are excluded even under All`() = runTest {
        val retiringCatalog = FakePlatformCatalog(
            listOf(
                FakePlatformCatalog.entry(igdbId = 6, name = "PC", order = 0),
                FakePlatformCatalog.entry(
                    igdbId = 48,
                    name = "PS4",
                    order = 1,
                    status = com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformStatus.RETIRED
                )
            )
        )
        val useCase = GetReleasesUseCase(fakeRepository, retiringCatalog)
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, name = "PC Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 48, name = "Retired Game"),
            Fixtures.gameReleaseRecord(releaseId = 3, gameId = 30, platformId = 9999, name = "Unknown Game")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters())).first() as Result.Success

        assertEquals(listOf("PC Game"), result.data.releases.map { it.name })
    }

    @Test
    fun `given records at the window boundaries when filtering then only in range records remain`() = runTest {
        val range = ReleaseDateRange(startMillis = 1_000L, endMillis = 2_000L)
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, releaseDate = Date(999L), name = "Before"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 6, releaseDate = Date(1_000L), name = "Start"),
            Fixtures.gameReleaseRecord(releaseId = 3, gameId = 30, platformId = 6, releaseDate = Date(2_000L), name = "End"),
            Fixtures.gameReleaseRecord(releaseId = 4, gameId = 40, platformId = 6, releaseDate = Date(2_001L), name = "After")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters(dateRange = range))).first() as Result.Success

        assertEquals(listOf("Start", "End"), result.data.releases.map { it.name })
    }

    @Test
    fun `given duplicate platform records in one group when grouped then labels are deduplicated`() = runTest {
        val day = Date(86_400_000L * 20_000L)
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, releaseDate = day, name = "Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 10, platformId = 6, releaseDate = day, name = "Game")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters())).first() as Result.Success

        assertEquals(listOf("PC"), result.data.releases.single().platforms)
    }

    @Test
    fun `given groups sharing a release day when sorted then chronological order uses a stable tie breaker`() = runTest {
        val day = Date(86_400_000L * 20_000L)
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 30, platformId = 6, releaseDate = day, name = "C"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 10, platformId = 6, releaseDate = day, name = "A"),
            Fixtures.gameReleaseRecord(releaseId = 3, gameId = 20, platformId = 6, releaseDate = day, name = "B")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters())).first() as Result.Success

        assertEquals(listOf(10L, 20L, 30L), result.data.releases.map { it.id })
    }

    @Test
    fun `given a loading result when invoke is called then it passes through unmodified`() = runTest {
        fakeRepository.releasesResultFlow.value = Result.Loading

        assertTrue(useCase(flowOf(filters())).first() is Result.Loading)
    }

    @Test
    fun `given an error result when invoke is called then it passes through unmodified`() = runTest {
        val exception = RuntimeException("down")
        fakeRepository.releasesResultFlow.value = Result.Error(exception)

        val result = useCase(flowOf(filters())).first() as Result.Error

        assertEquals(exception, result.exception)
    }

    @Test
    fun `given changing filters when they emit then cached records are re filtered without restarting the stream`() = runTest {
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, name = "PC Game"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 48, name = "PS4 Game")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)
        val filtersFlow = MutableStateFlow(filters())

        useCase(filtersFlow).test {
            assertEquals(2, (awaitItem() as Result.Success).data.releases.size)

            filtersFlow.value = filters(platformIds = setOf(6))

            val filtered = awaitItem() as Result.Success
            assertEquals(listOf("PC Game"), filtered.data.releases.map { it.name })
            assertEquals(1, fakeRepository.releasesStreamInvocations)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given grouped results when emitted then cards and count derive from the same result`() = runTest {
        val records = listOf(
            Fixtures.gameReleaseRecord(releaseId = 1, gameId = 10, platformId = 6, name = "Game A"),
            Fixtures.gameReleaseRecord(releaseId = 2, gameId = 20, platformId = 48, name = "Game B")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(records)

        val result = useCase(flowOf(filters())).first() as Result.Success<FilteredReleases>

        assertEquals(result.data.releases.size, result.data.matchCount)
    }
}
