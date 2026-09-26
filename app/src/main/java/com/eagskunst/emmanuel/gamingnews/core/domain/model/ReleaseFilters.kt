package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.util.Date

/** Closed date window (millis since epoch) used to restrict release results. */
data class ReleaseDateRange(
    val startMillis: Long,
    val endMillis: Long
) {
    operator fun contains(date: Date): Boolean = date.time in startMillis..endMillis
}

/**
 * Inputs for composable release filtering. Passed explicitly to the use case so background
 * consumers don't silently inherit Releases-tab preferences.
 *
 * [platformIds] is the set of selected IGDB platform IDs; an empty set means "All platforms".
 */
data class ReleaseFilters(
    val platformIds: Set<Int> = emptySet(),
    val searchQuery: String = "",
    val dateRange: ReleaseDateRange = UpcomingReleaseWindow.current()
)

/** Grouped release cards plus the count of matching loaded release groups. */
data class FilteredReleases(
    val releases: List<GameRelease>,
    val matchCount: Int
)
