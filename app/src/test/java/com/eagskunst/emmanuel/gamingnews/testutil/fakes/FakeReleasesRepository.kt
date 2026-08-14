package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Reusable [ReleasesRepository] fake backed by [MutableStateFlow]s, shared across all use
 * case/ViewModel/Compose UI tests that depend on game releases.
 *
 * [loadNextPageBlocker], when set, is awaited inside [loadNextPage] before returning, so tests
 * can exercise a "still in flight" state (e.g. a `loadMore()` guard) deterministically.
 */
class FakeReleasesRepository(
    initialResult: Result<List<GameRelease>> = Result.Success(emptyList()),
    initialHasMorePages: Boolean = true
) : ReleasesRepository {

    val releasesResultFlow = MutableStateFlow(initialResult)
    val hasMorePagesFlow = MutableStateFlow(initialHasMorePages)
    override val hasMorePages: StateFlow<Boolean> = hasMorePagesFlow

    var loadNextPageResult: Result<Boolean> = Result.Success(true)
    var loadNextPageBlocker: CompletableDeferred<Unit>? = null

    var releasesStreamInvocations = 0
        private set
    var loadNextPageInvocations = 0
        private set
    var lastForceRefresh: Boolean = false
        private set

    override fun releasesStream(forceRefresh: Boolean): Flow<Result<List<GameRelease>>> {
        releasesStreamInvocations++
        lastForceRefresh = forceRefresh
        return releasesResultFlow
    }

    override suspend fun loadNextPage(): Result<Boolean> {
        loadNextPageInvocations++
        loadNextPageBlocker?.await()
        return loadNextPageResult
    }
}
