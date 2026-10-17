package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.FollowedGameDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.FollowedGameEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * In-memory [FollowedGameDao] fake: REPLACE-on-conflict upsert and the same bitmask update
 * semantics as the real UPDATE statements.
 */
class FakeFollowedGameDao : FollowedGameDao {

    private val rowsFlow = MutableStateFlow<Map<Long, FollowedGameEntity>>(emptyMap())

    override fun observeIds(): Flow<List<Long>> = rowsFlow.map { it.keys.toList() }

    override suspend fun getAll(): List<FollowedGameEntity> = rowsFlow.value.values.toList()

    override suspend fun get(gameId: Long): FollowedGameEntity? = rowsFlow.value[gameId]

    override suspend fun upsert(game: FollowedGameEntity) {
        rowsFlow.value = rowsFlow.value + (game.gameId to game)
    }

    override suspend fun delete(gameId: Long) {
        rowsFlow.value = rowsFlow.value - gameId
    }

    override suspend fun deleteReleasedBefore(epochDay: Long) {
        rowsFlow.value = rowsFlow.value.filterValues { it.releaseEpochDay >= epochDay }
    }

    override suspend fun updateSync(
        gameId: Long,
        releaseEpochDay: Long,
        notifiedOffsets: Int,
        lastSyncedAt: Date?
    ) {
        rowsFlow.value[gameId]?.let { existing ->
            rowsFlow.value = rowsFlow.value + (gameId to existing.copy(
                releaseEpochDay = releaseEpochDay,
                notifiedOffsets = notifiedOffsets,
                lastSyncedAt = lastSyncedAt
            ))
        }
    }

    override suspend fun markNotified(gameId: Long, bit: Int) {
        rowsFlow.value[gameId]?.let { existing ->
            rowsFlow.value = rowsFlow.value + (gameId to existing.copy(
                notifiedOffsets = existing.notifiedOffsets or bit
            ))
        }
    }
}
