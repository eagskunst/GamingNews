package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toFollowedGame
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toFollowedGameEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.FollowedGameDao
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultFollowedGamesRepository @Inject constructor(
    private val followedGameDao: FollowedGameDao,
    private val dispatchers: DispatcherProvider
) : FollowedGamesRepository {

    override fun observeFollowedGameIds(): Flow<Set<Long>> =
        followedGameDao.observeIds()
            .map { ids -> ids.toSet() }
            .flowOn(dispatchers.io)

    override suspend fun getAll(): List<FollowedGame> = withContext(dispatchers.io) {
        followedGameDao.getAll().map { it.toFollowedGame() }
    }

    override suspend fun follow(game: FollowedGame) = withContext(dispatchers.io) {
        followedGameDao.upsert(game.toFollowedGameEntity())
    }

    override suspend fun unfollow(gameId: Long): FollowedGame? = withContext(dispatchers.io) {
        followedGameDao.get(gameId)?.toFollowedGame()
            .also { followedGameDao.delete(gameId) }
    }

    override suspend fun markNotified(gameId: Long, offset: ReminderOffset) =
        withContext(dispatchers.io) {
            followedGameDao.markNotified(gameId, offset.bit)
        }

    override suspend fun updateReleaseDate(gameId: Long, epochDay: Long) =
        withContext(dispatchers.io) {
            followedGameDao.updateSync(
                gameId = gameId,
                releaseEpochDay = epochDay,
                notifiedOffsets = 0,
                lastSyncedAt = Date()
            )
        }

    override suspend fun pruneReleasedBefore(epochDay: Long) = withContext(dispatchers.io) {
        followedGameDao.deleteReleasedBefore(epochDay)
    }
}
