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
}
