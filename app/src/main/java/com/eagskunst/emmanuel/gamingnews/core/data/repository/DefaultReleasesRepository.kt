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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date

class DefaultReleasesRepository(
    private val igdbRemoteDataSource: IgdbRemoteDataSource,
    private val releaseDao: ReleaseDao,
    private val dispatchers: DispatcherProvider
) : ReleasesRepository {

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

    private suspend fun refresh() {
        val dtos = igdbRemoteDataSource.fetchUpcomingReleases()
        val releases = mergeReleases(dtos)
        releaseDao.clear()
        releaseDao.insertAll(releases.map { it.toReleaseEntity() })
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
            .filter { it.releaseDate.isInCurrentMonth() }
            .sortedBy { it.releaseDate }
    }
}

private fun Date.isInCurrentMonth(): Boolean {
    val now = Calendar.getInstance()
    val release = Calendar.getInstance().apply { time = this@isInCurrentMonth }
    return now.get(Calendar.YEAR) == release.get(Calendar.YEAR) &&
        now.get(Calendar.MONTH) == release.get(Calendar.MONTH)
}
