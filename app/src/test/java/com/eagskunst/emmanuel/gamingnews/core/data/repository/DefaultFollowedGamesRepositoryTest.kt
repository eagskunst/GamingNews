package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFollowedGameDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFollowedGamesRepositoryTest {

    private val dao = FakeFollowedGameDao()
    private val repository = DefaultFollowedGamesRepository(
        followedGameDao = dao,
        dispatchers = TestDispatcherProvider()
    )

    @Test
    fun `given follows when ids are observed then the game id set is emitted`() = runTest {
        repository.follow(Fixtures.followedGame(gameId = 10))
        repository.follow(Fixtures.followedGame(gameId = 20))

        assertEquals(setOf(10L, 20L), repository.observeFollowedGameIds().first())
    }

    @Test
    fun `when a game is followed then it is returned by getAll`() = runTest {
        val game = Fixtures.followedGame(gameId = 10, name = "Followed Game")

        repository.follow(game)

        assertEquals(listOf(game), repository.getAll())
    }

    @Test
    fun `when a game is unfollowed then the removed row is returned`() = runTest {
        val game = Fixtures.followedGame(gameId = 10)
        repository.follow(game)

        val removed = repository.unfollow(10)

        assertEquals(game, removed)
        assertTrue(repository.getAll().isEmpty())
    }

    @Test
    fun `when an unknown game is unfollowed then null is returned`() = runTest {
        assertNull(repository.unfollow(999))
    }

    @Test
    fun `when markNotified runs then the offset bit is merged without clobbering others`() = runTest {
        repository.follow(
            Fixtures.followedGame(
                gameId = 10,
                offsets = ReminderOffset.entries.toSet(),
                notifiedOffsets = setOf(ReminderOffset.RELEASE_DAY)
            )
        )

        repository.markNotified(10, ReminderOffset.ONE_WEEK_BEFORE)

        val stored = repository.getAll().single()
        assertEquals(
            setOf(ReminderOffset.RELEASE_DAY, ReminderOffset.ONE_WEEK_BEFORE),
            stored.notifiedOffsets
        )
        assertEquals(ReminderOffset.entries.toSet(), stored.offsets)
    }

    @Test
    fun `when the release date is updated then notified offsets are reset`() = runTest {
        repository.follow(
            Fixtures.followedGame(
                gameId = 10,
                notifiedOffsets = setOf(ReminderOffset.RELEASE_DAY, ReminderOffset.ONE_DAY_BEFORE)
            )
        )

        repository.updateReleaseDate(10, 20_100)

        val stored = repository.getAll().single()
        assertEquals(20_100L, stored.releaseEpochDay)
        assertTrue(stored.notifiedOffsets.isEmpty())
    }

    @Test
    fun `when pruning then only games released before the day are removed`() = runTest {
        repository.follow(Fixtures.followedGame(gameId = 10, releaseEpochDay = 19_999))
        repository.follow(Fixtures.followedGame(gameId = 20, releaseEpochDay = 20_000))

        repository.pruneReleasedBefore(20_000)

        assertEquals(listOf(20L), repository.getAll().map { it.gameId })
    }
}
