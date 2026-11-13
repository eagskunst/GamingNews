package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toGameDetails
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.GameDetailsDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.GameDetailsEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.IgdbRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameDetailsDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameDetails
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.GameDetailsRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultGameDetailsRepository @Inject constructor(
    private val gameDetailsDao: GameDetailsDao,
    private val remoteDataSource: IgdbRemoteDataSource,
    private val catalog: PlatformCatalog,
    private val dispatchers: DispatcherProvider
) : GameDetailsRepository {

    /** Wall clock in epoch millis; replaced in tests to pin cache age. */
    internal var now: () -> Long = System::currentTimeMillis

    private val json = Json { ignoreUnknownKeys = true }

    override fun gameDetails(gameId: Long): Flow<Result<GameDetails>> = flow {
        val cached = gameDetailsDao.get(gameId)
        // A row written by an older app version may no longer decode; treat it as a miss.
        val cachedDetails = cached?.let {
            runCatching { json.decodeFromString<IgdbGameDetailsDto>(it.json) }.getOrNull()
                ?.toGameDetails(catalog)
        }
        emit(cachedDetails?.let { Result.Success(it) } ?: Result.Loading)

        val isFresh = cached != null && now() - cached.fetchedAt < STALE_AFTER_MILLIS
        if (cachedDetails != null && isFresh) return@flow
        try {
            val dto = remoteDataSource.fetchGameDetails(gameId)
            val details = dto?.toGameDetails(catalog)
                ?: throw NoSuchElementException("IGDB returned no game for id $gameId")
            gameDetailsDao.upsert(GameDetailsEntity(gameId, json.encodeToString(dto), now()))
            gameDetailsDao.deleteFetchedBefore(now() - RETENTION_MILLIS)
            emit(Result.Success(details))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            if (cachedDetails == null) emit(Result.Error(exception))
        }
    }.flowOn(dispatchers.io)

    private companion object {
        /** Scores, dates and hype move slowly; one refresh a day is plenty. */
        const val STALE_AFTER_MILLIS = 24L * 60 * 60 * 1000
        const val RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}
