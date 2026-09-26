package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toReleaseEntity
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toReleaseRecord
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ReleaseDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseCoverageEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameReleaseRecord
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReleaseDateRange
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UpcomingReleaseWindow
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultReleasesRepository @Inject constructor(
    private val igdbRemoteDataSource: IgdbRemoteDataSource,
    private val releaseDao: ReleaseDao,
    private val platformCatalog: PlatformCatalog,
    private val dispatchers: DispatcherProvider
) : ReleasesRepository {

    private val pageLimit = IgdbRemoteDataSource.PAGE_LIMIT
    private val operationMutex = Mutex()

    private val _hasMorePages = MutableStateFlow(true)
    override val hasMorePages: StateFlow<Boolean> = _hasMorePages

    override fun releasesStream(): Flow<Result<List<GameReleaseRecord>>> = flow {
        emit(Result.Loading)

        val context = coverageContext()
        val coverage = releaseDao.getCoverage()
        val cached = releaseDao.observeAll().first()
        val coverageStale = coverage == null || coverage.coverageKey != context.key
        if (coverageStale && coverage != null) {
            // The supported platform set or query window moved on; the old coverage no longer
            // describes a complete cache. Cached rows are preserved and keep being served
            // until fresh data is committed.
            releaseDao.upsertCoverage(coverage.copy(isComplete = false))
        }
        _hasMorePages.value = coverageStale || !(coverage?.isComplete ?: false)

        if (coverageStale || cached.isEmpty()) {
            try {
                operationMutex.withLock { fetchPage(offset = 0, replace = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }

        emitAll(
            releaseDao.observeAll().map { entities ->
                Result.Success(entities.map { it.toReleaseRecord() })
            }
        )
    }.catch { e ->
        emit(Result.Error(e))
    }.flowOn(dispatchers.io)

    override suspend fun refresh(): Result<Unit> = withContext(dispatchers.io) {
        try {
            operationMutex.withLock { fetchPage(offset = 0, replace = true) }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun loadNextPage(): Result<Boolean> = withContext(dispatchers.io) {
        if (!operationMutex.tryLock()) return@withContext Result.Success(_hasMorePages.value)
        try {
            val context = coverageContext()
            val coverage = releaseDao.getCoverage()
            when {
                coverage == null || coverage.coverageKey != context.key ->
                    Result.Success(fetchPage(offset = 0, replace = true))
                coverage.isComplete -> {
                    _hasMorePages.value = false
                    Result.Success(false)
                }
                else -> Result.Success(fetchPage(coverage.nextOffset, replace = false))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        } finally {
            operationMutex.unlock()
        }
    }

    /**
     * Fetches one page for the current supported-platform set and query window, then commits
     * records and coverage metadata together. Coverage is only marked complete when the page
     * comes back short, meaning every relevant page has been fetched.
     */
    private suspend fun fetchPage(offset: Int, replace: Boolean): Boolean {
        val context = coverageContext()
        val dtos = igdbRemoteDataSource.fetchUpcomingReleases(offset, context.window)
        val entities = dtos.mapNotNull { it.toReleaseRecord() }.map { it.toReleaseEntity() }
        val exhausted = dtos.size < pageLimit
        val coverage = ReleaseCoverageEntity(
            coverageKey = context.key,
            nextOffset = offset + dtos.size,
            isComplete = exhausted
        )
        if (replace) {
            releaseDao.replaceCache(entities, coverage)
        } else {
            releaseDao.commitPage(entities, coverage)
        }
        _hasMorePages.value = !exhausted
        return !exhausted
    }

    private fun coverageContext(): CoverageContext {
        val window = UpcomingReleaseWindow.current()
        return CoverageContext(
            key = UpcomingReleaseWindow.coverageKey(platformCatalog.supportedIds(), window),
            window = window
        )
    }

    private data class CoverageContext(val key: String, val window: ReleaseDateRange)
}
