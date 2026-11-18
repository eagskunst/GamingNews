package com.eagskunst.emmanuel.gamingnews.core.domain.model

import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FollowedGameTest {

    private val today = 20_000L

    @Test
    fun `given mixed follows when sorted then upcoming come soonest first and released most recent first`() {
        val games = listOf(
            Fixtures.followedGame(gameId = 1, releaseEpochDay = today - 30),
            Fixtures.followedGame(gameId = 2, releaseEpochDay = today + 10),
            Fixtures.followedGame(gameId = 3, releaseEpochDay = today - 1),
            Fixtures.followedGame(gameId = 4, releaseEpochDay = today),
            Fixtures.followedGame(gameId = 5, releaseEpochDay = today + 2)
        )

        val sorted = games.sortedForFollowedList(today)

        assertEquals(listOf(4L, 5L, 2L, 3L, 1L), sorted.map { it.gameId })
    }

    @Test
    fun `given a release day when compared to today then only strictly earlier days are released`() {
        assertFalse(Fixtures.followedGame(releaseEpochDay = today).isReleased(today))
        assertTrue(Fixtures.followedGame(releaseEpochDay = today - 1).isReleased(today))
    }

    @Test
    fun `given a release day when checked then it stays active through the seven day grace window`() {
        assertTrue(Fixtures.followedGame(releaseEpochDay = today + 5).isActive(today))
        assertTrue(Fixtures.followedGame(releaseEpochDay = today - 7).isActive(today))
        assertFalse(Fixtures.followedGame(releaseEpochDay = today - 8).isActive(today))
    }
}
