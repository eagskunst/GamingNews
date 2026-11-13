package com.eagskunst.emmanuel.gamingnews.core.domain.model

import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameDetailsTest {

    private val today = 20_000L
    private val supported = setOf(6, 167)

    @Test
    fun `given only past releases when pendingRelease then nothing can be followed`() {
        val details = Fixtures.gameDetails(
            platformReleases = listOf(Fixtures.platformRelease(epochDay = today - 1))
        )

        assertNull(details.pendingRelease(today, supported))
        assertNull(details.nextRelease(today))
    }

    @Test
    fun `given an old game with a new port when pendingRelease then the port is followable`() {
        val details = Fixtures.gameDetails(
            gameId = 7,
            platformReleases = listOf(
                Fixtures.platformRelease(platformId = 6, platformName = "PC", epochDay = today - 4_000),
                Fixtures.platformRelease(platformId = 167, platformName = "PS5", epochDay = today + 30)
            )
        )

        val pending = details.pendingRelease(today, supported)!!

        assertEquals(7L, pending.id)
        assertEquals(listOf("PS5"), pending.platforms)
        assertEquals((today + 30) * 86_400_000L, pending.releaseDate.time)
    }

    @Test
    fun `given upcoming releases that are approximate or untracked when pendingRelease then none qualifies`() {
        val details = Fixtures.gameDetails(
            platformReleases = listOf(
                Fixtures.platformRelease(platformId = 6, epochDay = today + 10, hasExactDate = false, human = "2028"),
                Fixtures.platformRelease(platformId = 14, platformName = "Mac", epochDay = today + 5)
            )
        )

        assertNull(details.pendingRelease(today, supported))
        // The countdown still has something to show: the nearest upcoming release.
        assertEquals("Mac", details.nextRelease(today)!!.platformName)
    }

    @Test
    fun `given several platforms launching the same day when pendingRelease then all are listed`() {
        val details = Fixtures.gameDetails(
            platformReleases = listOf(
                Fixtures.platformRelease(platformId = 6, platformName = "PC", epochDay = today),
                Fixtures.platformRelease(platformId = 167, platformName = "PS5", epochDay = today)
            )
        )

        assertEquals(listOf("PC", "PS5"), details.pendingRelease(today, supported)!!.platforms)
    }
}
