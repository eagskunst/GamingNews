package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toGameRelease
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toReleaseEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ReleaseDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class DefaultReleasesRepository @Inject constructor(
    private val igdbRemoteDataSource: IgdbRemoteDataSource,
    private val releaseDao: ReleaseDao,
    private val dispatchers: DispatcherProvider
) : ReleasesRepository {

    private val pageLimit = IgdbRemoteDataSource.PAGE_LIMIT
    private var currentOffset = 0

    private val _hasMorePages = MutableStateFlow(true)
    override val hasMorePages: StateFlow<Boolean> = _hasMorePages

    override fun releasesStream(forceRefresh: Boolean): Flow<Result<List<GameRelease>>> = flow {
        emit(Result.Loading)
        val isEmpty = releaseDao.observeAll().first().isEmpty()
        if (forceRefresh || isEmpty) {
            refresh()
        }
        emitAll(
            releaseDao.observeAll().map { entities ->
                Result.Success(entities.map { it.toGameRelease() })
            }
        )
    }.catch { e ->
        emit(Result.Error(e))
    }.flowOn(dispatchers.io)

    override suspend fun loadNextPage(): Result<Boolean> = withContext(dispatchers.io) {
        if (!_hasMorePages.value) {
            return@withContext Result.Success(false)
        }

        try {
            val dtos = igdbRemoteDataSource.fetchUpcomingReleases(currentOffset)
            val releases = mergeReleases(dtos)
            releaseDao.insertAll(releases.map { it.toReleaseEntity() })

            if (dtos.size < pageLimit) {
                _hasMorePages.value = false
            } else {
                currentOffset += pageLimit
                _hasMorePages.value = true
            }

            Result.Success(_hasMorePages.value)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun refresh() {
        currentOffset = 0
        _hasMorePages.value = true

        val dtos = igdbRemoteDataSource.fetchUpcomingReleases(0)
        val releases = mergeReleases(dtos)
        releaseDao.clear()
        releaseDao.insertAll(releases.map { it.toReleaseEntity() })

        if (dtos.size < pageLimit) {
            _hasMorePages.value = false
        } else {
            currentOffset = pageLimit
            _hasMorePages.value = true
        }
    }

    private fun mergeReleases(dtos: List<IgdbReleaseDateDto>): List<GameRelease> {
        val mapped = dtos.mapNotNull { it.toGameRelease() }
        val merged = mutableMapOf<Long, GameRelease>()
        for (release in mapped) {
            val existing = merged[release.id]
            if (existing == null) {
                merged[release.id] = release
            } else {
                val combinedPlatforms = (existing.platforms + release.platforms).distinct()
                merged[release.id] = existing.copy(platforms = combinedPlatforms)
            }
        }
        return merged.values
            .filter { it.releaseDate.isWithinUpcomingWindow() }
            .sortedBy { it.releaseDate }
    }
}

private fun Date.isWithinUpcomingWindow(): Boolean {
    if (this.before(startOfToday())) return false

    val now = Calendar.getInstance()
    val endOfCurrentYear = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.DECEMBER)
        set(Calendar.DAY_OF_MONTH, 31)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val eightMonthsFromNow = Calendar.getInstance().apply {
        add(Calendar.MONTH, 8)
    }

    val maxDate = if (endOfCurrentYear.before(eightMonthsFromNow)) {
        endOfCurrentYear
    } else {
        eightMonthsFromNow
    }

    return !this.after(maxDate.time)
}

private fun startOfToday(): Date {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}
