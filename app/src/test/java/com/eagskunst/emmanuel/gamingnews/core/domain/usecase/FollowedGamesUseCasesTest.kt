package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFollowedGamesRepository
import com.eagskunst.emmanuel.gamingnews.worker.GameReleaseScheduler
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FollowedGamesUseCasesTest {

    private val repository = FakeFollowedGamesRepository()
    private val scheduler: GameReleaseScheduler = mockk(relaxed = true)

    private val followGame = FollowGameUseCase(repository, scheduler)
    private val unfollowGame = UnfollowGameUseCase(repository, scheduler)

    @Test
    fun `given an exact release when followed then the game is stored and the check scheduled`() = runTest {
        val release = Fixtures.gameRelease(
            id = 42,
            name = "Exact Game",
            releaseDate = Date(20_000L * 86_400_000L),
            platforms = listOf("PC", "PS5")
        )

        val result = followGame(release, setOf(ReminderOffset.RELEASE_DAY))

        assertTrue(result is Result.Success)
        val stored = repository.getAll().single()
        assertEquals(42L, stored.gameId)
        assertEquals(20_000L, stored.releaseEpochDay)
        assertEquals("PC · PS5", stored.platformsLabel)
        assertEquals(setOf(ReminderOffset.RELEASE_DAY), stored.offsets)
        verify { scheduler.ensureScheduled() }
    }

    @Test
    fun `given an approximate release when followed then the follow is rejected`() = runTest {
        val release = Fixtures.gameRelease(hasExactDate = false, human = "TBA")

        val result = followGame(release, setOf(ReminderOffset.RELEASE_DAY))

        assertTrue(result is Result.Error)
        assertTrue(repository.getAll().isEmpty())
        verify(exactly = 0) { scheduler.ensureScheduled() }
    }

    @Test
    fun `given no offsets when followed then the follow is rejected`() = runTest {
        val result = followGame(Fixtures.gameRelease(), emptySet())

        assertTrue(result is Result.Error)
        assertTrue(repository.getAll().isEmpty())
    }

    @Test
    fun `given a followed game when unfollowed then the removed game is returned for undo`() = runTest {
        val game = Fixtures.followedGame(gameId = 42)
        repository.follow(game)

        val removed = unfollowGame(42)

        assertEquals(game, removed)
        assertTrue(repository.getAll().isEmpty())
        coVerify { scheduler.cancelIfNoneFollowed() }
    }

    @Test
    fun `given ids are observed when a follow changes then the emitted set tracks it`() = runTest {
        repository.follow(Fixtures.followedGame(gameId = 10))
        val observeIds = ObserveFollowedGameIdsUseCase(repository)

        assertEquals(setOf(10L), observeIds().first())
    }

    @Test
    fun `given a removed follow when restored then it is stored again and rescheduled`() = runTest {
        val game = Fixtures.followedGame(gameId = 42)

        followGame.restore(game)

        assertEquals(listOf(game), repository.getAll())
        verify { scheduler.ensureScheduled() }
    }
}
