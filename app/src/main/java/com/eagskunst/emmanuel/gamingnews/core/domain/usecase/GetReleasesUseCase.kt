package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.common.map
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredReleases
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameReleaseRecord
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReleaseFilters
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.util.Date

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * Combines the unfiltered release-record stream, explicit [ReleaseFilters] and the platform
 * catalog, then:
 *
 * 1. restricts records to supported platforms and the filter's date window,
 * 2. applies platform OR matching,
 * 3. applies case-insensitive name search,
 * 4. groups by game + UTC calendar date,
 * 5. deduplicates platform labels within each group,
 * 6. sorts chronologically with the game ID as a stable tie-breaker.
 *
 * The unfiltered cache observation is shared and never restarted when filters change.
 */
class GetReleasesUseCase(
    private val repository: ReleasesRepository,
    private val catalog: PlatformCatalog
) {
    operator fun invoke(
        filters: Flow<ReleaseFilters> = flowOf(ReleaseFilters())
    ): Flow<Result<FilteredReleases>> =
        combine(repository.releasesStream(), filters) { result, filter ->
            result.map { records -> applyFilters(records, filter) }
        }

    private fun applyFilters(
        records: List<GameReleaseRecord>,
        filters: ReleaseFilters
    ): FilteredReleases {
        val supportedIds = catalog.supportedIds()
        val query = filters.searchQuery.trim()
        val grouped = records.asSequence()
            .filter { it.platformId in supportedIds && it.releaseDate in filters.dateRange }
            .filter { filters.platformIds.isEmpty() || it.platformId in filters.platformIds }
            .filter { query.isEmpty() || it.name.contains(query, ignoreCase = true) }
            .groupBy { it.gameId to utcEpochDay(it.releaseDate) }
            .map { (key, group) -> toReleaseGroup(key, group) }
            .sortedWith(compareBy({ it.releaseDate.time }, { it.id }))
        return FilteredReleases(releases = grouped, matchCount = grouped.size)
    }

    private fun toReleaseGroup(key: Pair<Long, Long>, group: List<GameReleaseRecord>): GameRelease {
        val platformLabels = group.map { it.platformId }
            .distinct()
            .sortedBy { catalog.find(it)?.displayOrder ?: Int.MAX_VALUE }
            .map { catalog.displayName(it) }
            .distinct()
        return GameRelease(
            id = key.first,
            name = group.first().name,
            coverUrl = group.firstNotNullOfOrNull { it.coverUrl },
            releaseDate = Date(key.second * MILLIS_PER_DAY),
            platforms = platformLabels,
            gameUrl = group.firstNotNullOfOrNull { it.gameUrl }
        )
    }

    /** UTC calendar day of an instant, so midnight UTC timestamps don't shift a day back. */
    private fun utcEpochDay(date: Date): Long = Math.floorDiv(date.time, MILLIS_PER_DAY)
}
