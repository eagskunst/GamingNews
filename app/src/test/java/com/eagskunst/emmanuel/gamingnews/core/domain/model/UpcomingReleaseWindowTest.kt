package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UpcomingReleaseWindowTest {

    private fun millis(iso: String): Long = Instant.parse(iso).toEpochMilli()

    @Test
    fun `given a moment mid year when computing the window then it starts at the beginning of the UTC day`() {
        val window = UpcomingReleaseWindow.current(millis("2024-03-15T10:30:00Z"))

        assertEquals(millis("2024-03-15T00:00:00Z"), window.startMillis)
    }

    @Test
    fun `given a moment whose eight month horizon ends before year end when computing the window then it ends eight months out`() {
        val window = UpcomingReleaseWindow.current(millis("2024-03-15T10:30:00Z"))

        assertEquals(millis("2024-11-15T10:30:00Z"), window.endMillis)
    }

    @Test
    fun `given a moment whose eight month horizon passes year end when computing the window then it ends at end of year`() {
        val window = UpcomingReleaseWindow.current(millis("2024-06-15T10:30:00Z"))

        assertEquals(millis("2024-12-31T23:59:59.999Z"), window.endMillis)
    }

    @Test
    fun `given the same platform ids in different order when building the coverage key then the keys match`() {
        val window = ReleaseDateRange(startMillis = 1L, endMillis = 2L)

        val a = UpcomingReleaseWindow.coverageKey(setOf(6, 48, 167), window)
        val b = UpcomingReleaseWindow.coverageKey(setOf(167, 6, 48), window)

        assertEquals(a, b)
    }

    @Test
    fun `given different platform sets when building the coverage key then the keys differ`() {
        val window = ReleaseDateRange(startMillis = 1L, endMillis = 2L)

        val seven = UpcomingReleaseWindow.coverageKey(setOf(6, 48, 167, 49, 169, 130, 508), window)
        val eight = UpcomingReleaseWindow.coverageKey(setOf(6, 48, 167, 49, 169, 130, 508, 33), window)

        assertNotEquals(seven, eight)
    }

    @Test
    fun `given different windows when building the coverage key then the keys differ`() {
        val ids = setOf(6, 48)

        val a = UpcomingReleaseWindow.coverageKey(ids, ReleaseDateRange(1L, 2L))
        val b = UpcomingReleaseWindow.coverageKey(ids, ReleaseDateRange(1L, 3L))

        assertNotEquals(a, b)
    }
}
