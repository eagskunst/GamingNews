package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameReleaseRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ReleasesRepository {

    /**
     * Unfiltered stream of cached release records. Emits [Result.Loading] once, refreshes the
     * cache when it's empty or its coverage no longer matches the supported platform set and
     * query window, and keeps observing the cache afterwards — including after a failed
     * refresh, so local filtering stays reactive offline.
     */
    fun releasesStream(): Flow<Result<List<GameReleaseRecord>>>

    /** Fetches the first page and transactionally replaces the release cache. */
    suspend fun refresh(): Result<Unit>

    /**
     * Fetches the next page for the current coverage. Returns [Result.Success] with `true`
     * when more pages remain. Calls overlapping another in-flight operation return
     * immediately instead of duplicating a request.
     */
    suspend fun loadNextPage(): Result<Boolean>

    /** Whether more pages remain for the current coverage. */
    val hasMorePages: StateFlow<Boolean>

    /**
     * Exact (day-precision) release day per game ID, expressed as a UTC epoch day: the
     * IGDB release row closest to each stored day in [storedDays]. Games with no exact
     * date left are absent from the map. Does not touch the release cache.
     * [notBeforeEpochSeconds] bounds the remote query below so historical per-platform
     * releases of long-lived games can't win the match.
     */
    suspend fun fetchExactReleaseDays(
        storedDays: Map<Long, Long>,
        notBeforeEpochSeconds: Long
    ): Result<Map<Long, Long>>
}
