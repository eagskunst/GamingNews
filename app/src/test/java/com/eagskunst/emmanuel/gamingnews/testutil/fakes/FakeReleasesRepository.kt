package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameReleaseRecord
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Reusable [ReleasesRepository] fake backed by [MutableStateFlow]s, shared across all use
 * case/ViewModel/Compose UI tests that depend on game release records.
 *
 * [loadNextPageBlocker], when set, is awaited inside [loadNextPage] before returning, so tests
 * can exercise a "still in flight" state (e.g. a `loadMore()` guard) deterministically.
 */
class FakeReleasesRepository(
    initialResult: Result<List<GameReleaseRecord>> = Result.Success(emptyList()),
    initialHasMorePages: Boolean = false
) : ReleasesRepository {

    val releasesResultFlow = MutableStateFlow(initialResult)
    val hasMorePagesFlow = MutableStateFlow(initialHasMorePages)
    override val hasMorePages: StateFlow<Boolean> = hasMorePagesFlow

    /** Returned by [loadNextPage] once [loadNextPageResults] is drained. */
    var loadNextPageResult: Result<Boolean> = Result.Success(false)

    /** Results consumed in order by [loadNextPage], for multi-page scenarios. */
    val loadNextPageResults = ArrayDeque<Result<Boolean>>()

    var loadNextPageBlocker: CompletableDeferred<Unit>? = null
    var refreshResult: Result<Unit> = Result.Success(Unit)
    var refreshBlocker: CompletableDeferred<Unit>? = null

    var releasesStreamInvocations = 0
        private set
    var loadNextPageInvocations = 0
        private set
    var refreshInvocations = 0
        private set

    override fun releasesStream(): Flow<Result<List<GameReleaseRecord>>> {
        releasesStreamInvocations++
        return releasesResultFlow
    }

    override suspend fun refresh(): Result<Unit> {
        refreshInvocations++
        refreshBlocker?.await()
        return refreshResult
    }

    override suspend fun loadNextPage(): Result<Boolean> {
        loadNextPageInvocations++
        loadNextPageBlocker?.await()
        val result = if (loadNextPageResults.isNotEmpty()) {
            loadNextPageResults.removeFirst()
        } else {
            loadNextPageResult
        }
        if (result is Result.Success) hasMorePagesFlow.value = result.data
        return result
    }
}
