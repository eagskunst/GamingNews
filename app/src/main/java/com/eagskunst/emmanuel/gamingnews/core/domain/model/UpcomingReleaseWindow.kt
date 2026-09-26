package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset

/**
 * Computes the "upcoming releases" date window shared by the IGDB query and local filtering.
 *
 * IGDB release dates are Unix-seconds timestamps, so the window is anchored in UTC: the start
 * is the beginning of today (UTC) and the end is the earlier of the end of the current year
 * (UTC) or eight months from now.
 */
object UpcomingReleaseWindow {

    private val UTC = ZoneOffset.UTC

    fun current(nowMillis: Long = System.currentTimeMillis()): ReleaseDateRange {
        val now = Instant.ofEpochMilli(nowMillis).atZone(UTC)
        val start = now.toLocalDate().atStartOfDay(UTC).toInstant().toEpochMilli()
        val endOfYear = LocalDate.of(now.year, Month.DECEMBER, 31)
            .atTime(LocalTime.MAX)
            .atZone(UTC)
            .toInstant()
            .toEpochMilli()
        val eightMonthsAhead = now.plusMonths(8).toInstant().toEpochMilli()
        return ReleaseDateRange(
            startMillis = start,
            endMillis = minOf(endOfYear, eightMonthsAhead)
        )
    }

    /**
     * Stable key describing which platform set + query window cached data covers.
     * Derived only from supported IDs and window bounds — never display labels or ordering —
     * so renames and reorders don't invalidate the cache.
     */
    fun coverageKey(platformIds: Set<Int>, range: ReleaseDateRange): String =
        buildString {
            append(platformIds.sorted().joinToString(","))
            append('|')
            append(range.startMillis)
            append('|')
            append(range.endMillis)
        }
}
