package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameDetailsDto
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeGameDetailsDao
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class DefaultGameDetailsRepositoryTest {

    private val dao = FakeGameDetailsDao()
    private val remote: IgdbRemoteDataSource = mockk()
    private var clock = 1_000_000_000_000L
    private val repository = DefaultGameDetailsRepository(
        gameDetailsDao = dao,
        remoteDataSource = remote,
        catalog = FakePlatformCatalog(),
        dispatchers = TestDispatcherProvider()
    ).apply { now = { clock } }

    private fun names(results: List<Result<*>>) = results.map {
        (it as? Result.Success)?.data?.let { data ->
            (data as com.eagskunst.emmanuel.gamingnews.core.domain.model.GameDetails).name
        } ?: it::class.simpleName
    }

    @Test
    fun `given no cache when details are requested then loading is followed by the fetched game and it is cached`() = runTest {
        coEvery { remote.fetchGameDetails(1) } returns IgdbGameDetailsDto(id = 1, name = "Fresh")

        val results = repository.gameDetails(1).toList()

        assertEquals(listOf("Loading", "Fresh"), names(results))
        assertEquals(clock, dao.rows.getValue(1).fetchedAt)
    }

    @Test
    fun `given a fresh cache when details are requested then IGDB is not called`() = runTest {
        coEvery { remote.fetchGameDetails(1) } returns IgdbGameDetailsDto(id = 1, name = "Cached")
        repository.gameDetails(1).toList()
        clock += HOUR_MILLIS

        val results = repository.gameDetails(1).toList()

        assertEquals(listOf("Cached"), names(results))
        coVerify(exactly = 1) { remote.fetchGameDetails(1) }
    }

    @Test
    fun `given a stale cache when details are requested then the cached copy shows first and is refreshed`() = runTest {
        coEvery { remote.fetchGameDetails(1) } returns IgdbGameDetailsDto(id = 1, name = "Old")
        repository.gameDetails(1).toList()
        clock += 25 * HOUR_MILLIS
        coEvery { remote.fetchGameDetails(1) } returns IgdbGameDetailsDto(id = 1, name = "New")

        val results = repository.gameDetails(1).toList()

        assertEquals(listOf("Old", "New"), names(results))
    }

    @Test
    fun `given a stale cache when the refresh fails then the cached copy is kept without an error`() = runTest {
        coEvery { remote.fetchGameDetails(1) } returns IgdbGameDetailsDto(id = 1, name = "Old")
        repository.gameDetails(1).toList()
        clock += 25 * HOUR_MILLIS
        coEvery { remote.fetchGameDetails(1) } throws IOException("offline")

        val results = repository.gameDetails(1).toList()

        assertEquals(listOf("Old"), names(results))
    }

    @Test
    fun `given no cache when the fetch fails or finds nothing then an error is emitted`() = runTest {
        coEvery { remote.fetchGameDetails(1) } throws IOException("offline")
        coEvery { remote.fetchGameDetails(2) } returns null

        assertTrue(repository.gameDetails(1).toList().last() is Result.Error)
        assertTrue(repository.gameDetails(2).toList().last() is Result.Error)
    }

    @Test
    fun `given an old cache row when another game is fetched then the old row is pruned`() = runTest {
        coEvery { remote.fetchGameDetails(any()) } answers { IgdbGameDetailsDto(id = firstArg(), name = "Game") }
        repository.gameDetails(1).toList()
        clock += 31 * 24 * HOUR_MILLIS

        repository.gameDetails(2).toList()

        assertEquals(setOf(2L), dao.rows.keys)
    }

    private companion object {
        const val HOUR_MILLIS = 60L * 60 * 1000
    }
}
