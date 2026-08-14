package com.eagskunst.emmanuel.gamingnews.core.data.repository

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toReleaseEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleaseDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultReleasesRepositoryTest {

    private val remoteDataSource: IgdbRemoteDataSource = mockk()
    private val releaseDao = FakeReleaseDao()
    private val repository = DefaultReleasesRepository(
        igdbRemoteDataSource = remoteDataSource,
        releaseDao = releaseDao,
        dispatchers = TestDispatcherProvider()
    )

    @Test
    fun `releasesStream with forceRefresh true refreshes from remote and emits mapped releases`() = runTest {
        val dto = releaseDto(id = 1, gameId = 100, name = "Test Game", platform = 6, dateOffsetSeconds = 3600)

        coEvery { remoteDataSource.fetchUpcomingReleases(0) } returns listOf(dto)

        repository.releasesStream(forceRefresh = true).test {
            assertTrue(awaitItem() is Result.Loading)

            val result = awaitItem() as Result.Success
            assertEquals(listOf("Test Game"), result.data.map { it.name })
            assertEquals(listOf(listOf("PC")), result.data.map { it.platforms })

            cancelAndConsumeRemainingEvents()
        }

        coVerify { remoteDataSource.fetchUpcomingReleases(0) }
        assertEquals(1, releaseDao.observeAll().first().size)
    }

    @Test
    fun `releasesStream with forceRefresh false when dao has data does not refresh`() = runTest {
        val storedRelease = Fixtures.gameRelease(id = 1L, name = "Stored Game")
        releaseDao.insertAll(listOf(storedRelease.toReleaseEntity()))

        repository.releasesStream(forceRefresh = false).test {
            assertTrue(awaitItem() is Result.Loading)

            val result = awaitItem() as Result.Success
            assertEquals(listOf("Stored Game"), result.data.map { it.name })

            cancelAndConsumeRemainingEvents()
        }

        coVerify(exactly = 0) { remoteDataSource.fetchUpcomingReleases(any()) }
    }

    @Test
    fun `loadNextPage fetches next offset appends results and sets hasMorePages to false when page is smaller than limit`() = runTest {
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

        coEvery { remoteDataSource.fetchUpcomingReleases(0) } returns fullPage
        coEvery { remoteDataSource.fetchUpcomingReleases(IgdbRemoteDataSource.PAGE_LIMIT) } returns lastPage

        assertTrue(repository.hasMorePages.value)

        val firstPageResult = repository.loadNextPage()
        assertEquals(Result.Success(true), firstPageResult)
        assertTrue(repository.hasMorePages.value)
        assertEquals(IgdbRemoteDataSource.PAGE_LIMIT, releaseDao.observeAll().first().size)

        val secondPageResult = repository.loadNextPage()
        assertEquals(Result.Success(false), secondPageResult)
        assertFalse(repository.hasMorePages.value)
        assertEquals(IgdbRemoteDataSource.PAGE_LIMIT + 1, releaseDao.observeAll().first().size)

        coVerify { remoteDataSource.fetchUpcomingReleases(0) }
        coVerify { remoteDataSource.fetchUpcomingReleases(IgdbRemoteDataSource.PAGE_LIMIT) }
    }

    @Test
    fun `refresh merges releases with the same game id combining distinct platforms`() = runTest {
        val pcRelease = releaseDto(id = 1, gameId = 100, name = "Shared Game", platform = 6, dateOffsetSeconds = 3600)
        val xboxRelease = releaseDto(id = 2, gameId = 100, name = "Shared Game", platform = 49, dateOffsetSeconds = 7200)

        coEvery { remoteDataSource.fetchUpcomingReleases(0) } returns listOf(pcRelease, xboxRelease)

        repository.releasesStream(forceRefresh = true).test {
            assertTrue(awaitItem() is Result.Loading)

            val result = awaitItem() as Result.Success
            assertEquals(1, result.data.size)
            assertEquals(listOf("PC", "Xbox One"), result.data.single().platforms)

            cancelAndConsumeRemainingEvents()
        }
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
