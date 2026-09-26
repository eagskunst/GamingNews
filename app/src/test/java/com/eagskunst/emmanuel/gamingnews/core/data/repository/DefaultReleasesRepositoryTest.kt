package com.eagskunst.emmanuel.gamingnews.core.data.repository

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseCoverageEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UpcomingReleaseWindow
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleaseDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultReleasesRepositoryTest {

    private val remoteDataSource: IgdbRemoteDataSource = mockk()
    private val releaseDao = FakeReleaseDao()
    private val platformCatalog = FakePlatformCatalog()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = DefaultReleasesRepository(
        igdbRemoteDataSource = remoteDataSource,
        releaseDao = releaseDao,
        platformCatalog = platformCatalog,
        dispatchers = TestDispatcherProvider(testDispatcher)
    )

    private fun currentCoverageKey(): String = UpcomingReleaseWindow.coverageKey(
        platformCatalog.supportedIds(),
        UpcomingReleaseWindow.current()
    )

    @Test
    fun `given an empty cache when the stream is collected then it refreshes and emits release records`() = runTest {
        val dto = releaseDto(id = 1, gameId = 100, name = "Test Game", platform = 6, dateOffsetSeconds = 3600)
        coEvery { remoteDataSource.fetchUpcomingReleases(0, any()) } returns listOf(dto)

        repository.releasesStream().test {
            assertTrue(awaitItem() is Result.Loading)

            val result = awaitItem() as Result.Success
            assertEquals(listOf("Test Game"), result.data.map { it.name })
            assertEquals(listOf(1L), result.data.map { it.releaseId })
            assertEquals(listOf(100L), result.data.map { it.gameId })
            assertEquals(listOf(6), result.data.map { it.platformId })

            cancelAndConsumeRemainingEvents()
        }

        coVerify { remoteDataSource.fetchUpcomingReleases(0, any()) }
        val coverage = releaseDao.getCoverage()!!
        assertTrue(coverage.isComplete)
        assertFalse(repository.hasMorePages.value)
    }

    @Test
    fun `given cached records and complete coverage when the stream is collected then no refresh happens`() = runTest {
        releaseDao.insertAll(listOf(Fixtures.releaseEntity(name = "Stored Game")))
        releaseDao.upsertCoverage(
            ReleaseCoverageEntity(coverageKey = currentCoverageKey(), nextOffset = 50, isComplete = true)
        )

        repository.releasesStream().test {
            assertTrue(awaitItem() is Result.Loading)

            val result = awaitItem() as Result.Success
            assertEquals(listOf("Stored Game"), result.data.map { it.name })

            cancelAndConsumeRemainingEvents()
        }

        coVerify(exactly = 0) { remoteDataSource.fetchUpcomingReleases(any(), any()) }
        assertFalse(repository.hasMorePages.value)
    }

    @Test
    fun `given stale coverage when the stream is collected then it marks coverage incomplete and refreshes`() = runTest {
        releaseDao.insertAll(listOf(Fixtures.releaseEntity(name = "Old Game")))
        releaseDao.upsertCoverage(
            ReleaseCoverageEntity(coverageKey = "old-key", nextOffset = 50, isComplete = true)
        )
        val dto = releaseDto(id = 1, gameId = 100, name = "New Game", platform = 6, dateOffsetSeconds = 3600)
        coEvery { remoteDataSource.fetchUpcomingReleases(0, any()) } returns listOf(dto)

        repository.releasesStream().test {
            assertTrue(awaitItem() is Result.Loading)
            val result = awaitItem() as Result.Success
            assertEquals(listOf("New Game"), result.data.map { it.name })
            cancelAndConsumeRemainingEvents()
        }

        coVerify { remoteDataSource.fetchUpcomingReleases(0, any()) }
        assertEquals(currentCoverageKey(), releaseDao.getCoverage()?.coverageKey)
    }

    @Test
    fun `given cached records when the refresh fails then the error is emitted and the cache keeps flowing`() = runTest {
        val stored = Fixtures.releaseEntity(id = 1L, name = "Stored Game")
        releaseDao.insertAll(listOf(stored))
        coEvery { remoteDataSource.fetchUpcomingReleases(0, any()) } throws IllegalStateException("offline")

        repository.releasesStream().test {
            assertTrue(awaitItem() is Result.Loading)
            assertTrue(awaitItem() is Result.Error)

            val cached = awaitItem() as Result.Success
            assertEquals(listOf("Stored Game"), cached.data.map { it.name })

            val extra = Fixtures.releaseEntity(id = 2L, name = "Later Game")
            releaseDao.insertAll(listOf(extra))
            val updated = awaitItem() as Result.Success
            assertEquals(listOf("Stored Game", "Later Game"), updated.data.map { it.name })

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `given full then short pages when loadNextPage is called then records append and coverage completes`() = runTest {
        val fullPage = List(IgdbRemoteDataSource.PAGE_LIMIT) { index ->
            releaseDto(
                id = index.toLong(),
                gameId = (1000 + index).toLong(),
                name = "Game $index",
                platform = 6,
                dateOffsetSeconds = 3600L + index
            )
        }
        val lastPage = listOf(
            releaseDto(id = 999L, gameId = 9999L, name = "Last Game", platform = 48, dateOffsetSeconds = 7200)
        )
        coEvery { remoteDataSource.fetchUpcomingReleases(0, any()) } returns fullPage
        coEvery { remoteDataSource.fetchUpcomingReleases(IgdbRemoteDataSource.PAGE_LIMIT, any()) } returns lastPage

        assertEquals(Result.Success(true), repository.loadNextPage())
        assertTrue(repository.hasMorePages.value)
        assertEquals(IgdbRemoteDataSource.PAGE_LIMIT, releaseDao.observeAll().first().size)
        assertFalse(releaseDao.getCoverage()!!.isComplete)
        assertEquals(IgdbRemoteDataSource.PAGE_LIMIT, releaseDao.getCoverage()!!.nextOffset)

        assertEquals(Result.Success(false), repository.loadNextPage())
        assertFalse(repository.hasMorePages.value)
        assertEquals(IgdbRemoteDataSource.PAGE_LIMIT + 1, releaseDao.observeAll().first().size)
        assertTrue(releaseDao.getCoverage()!!.isComplete)

        coVerify { remoteDataSource.fetchUpcomingReleases(0, any()) }
        coVerify { remoteDataSource.fetchUpcomingReleases(IgdbRemoteDataSource.PAGE_LIMIT, any()) }
    }

    @Test
    fun `given platform variants of one game across pages when fetched then every record survives`() = runTest {
        val pcRelease = releaseDto(id = 1, gameId = 100, name = "Shared Game", platform = 6, dateOffsetSeconds = 3600)
        val xboxRelease = releaseDto(id = 2, gameId = 100, name = "Shared Game", platform = 49, dateOffsetSeconds = 7200)
        // A full first page keeps coverage incomplete so the second page is fetched.
        val fillers = List(IgdbRemoteDataSource.PAGE_LIMIT - 1) { index ->
            releaseDto(
                id = (100 + index).toLong(),
                gameId = (2000 + index).toLong(),
                name = "Filler $index",
                platform = 167,
                dateOffsetSeconds = 600L + index
            )
        }

        coEvery { remoteDataSource.fetchUpcomingReleases(0, any()) } returns listOf(pcRelease) + fillers
        coEvery { remoteDataSource.fetchUpcomingReleases(IgdbRemoteDataSource.PAGE_LIMIT, any()) } returns listOf(xboxRelease)

        repository.loadNextPage()
        repository.loadNextPage()

        val stored = releaseDao.observeAll().first().associateBy { it.id }
        val pc = stored.getValue(1L)
        val xbox = stored.getValue(2L)
        assertEquals(6, pc.platformId)
        assertEquals(49, xbox.platformId)
        assertEquals(100L, pc.gameId)
        assertEquals(100L, xbox.gameId)
        assertTrue("platform variants must keep their own release dates", pc.releaseDate != xbox.releaseDate)
    }

    @Test
    fun `given an operation in flight when loadNextPage is called then it returns without duplicating a request`() = runTest {
        val blocker = CompletableDeferred<Unit>()
        coEvery { remoteDataSource.fetchUpcomingReleases(any(), any()) } coAnswers {
            blocker.await()
            emptyList()
        }

        val refreshJob = launch { repository.refresh() }
        runCurrent() // let the refresh start and suspend inside the remote fetch

        val result = repository.loadNextPage()

        assertEquals(Result.Success(true), result)
        blocker.complete(Unit)
        testDispatcher.scheduler.advanceUntilIdle()
        runCurrent()
        refreshJob.join()
        coVerify(exactly = 1) { remoteDataSource.fetchUpcomingReleases(any(), any()) }
    }

    @Test
    fun `given cached records when refresh is called then the cache is replaced transactionally`() = runTest {
        releaseDao.insertAll(listOf(Fixtures.releaseEntity(id = 1L, name = "Old Game")))
        val dto = releaseDto(id = 2, gameId = 200, name = "Fresh Game", platform = 6, dateOffsetSeconds = 3600)
        coEvery { remoteDataSource.fetchUpcomingReleases(0, any()) } returns listOf(dto)

        val result = repository.refresh()

        assertTrue(result is Result.Success)
        assertEquals(listOf("Fresh Game"), releaseDao.observeAll().first().map { it.name })
        assertEquals(currentCoverageKey(), releaseDao.getCoverage()?.coverageKey)
    }

    @Test
    fun `given a failed refresh when it is attempted then the cache and coverage are left untouched`() = runTest {
        releaseDao.insertAll(listOf(Fixtures.releaseEntity(id = 1L, name = "Old Game")))
        releaseDao.upsertCoverage(
            ReleaseCoverageEntity(coverageKey = "stale", nextOffset = 50, isComplete = false)
        )
        coEvery { remoteDataSource.fetchUpcomingReleases(0, any()) } throws IllegalStateException("offline")

        val result = repository.refresh()

        assertTrue(result is Result.Error)
        assertEquals(listOf("Old Game"), releaseDao.observeAll().first().map { it.name })
        assertEquals("stale", releaseDao.getCoverage()?.coverageKey)
        assertFalse(releaseDao.getCoverage()!!.isComplete)
    }

    @Test
    fun `given completed coverage when loadNextPage is called then it does not fetch again`() = runTest {
        releaseDao.insertAll(listOf(Fixtures.releaseEntity()))
        releaseDao.upsertCoverage(
            ReleaseCoverageEntity(coverageKey = currentCoverageKey(), nextOffset = 50, isComplete = true)
        )

        val result = repository.loadNextPage()

        assertEquals(Result.Success(false), result)
        coVerify(exactly = 0) { remoteDataSource.fetchUpcomingReleases(any(), any()) }
    }

    private fun releaseDto(
        id: Long,
        gameId: Long,
        name: String,
        platform: Int,
        dateOffsetSeconds: Long
    ): IgdbReleaseDateDto = IgdbReleaseDateDto(
        id = id,
        date = System.currentTimeMillis() / 1000 + dateOffsetSeconds,
        human = null,
        platform = platform,
        game = IgdbGameDto(
            id = gameId,
            name = name,
            url = null,
            cover = null
        )
    )
}
