package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [FollowedGamesRepository] fake driving followed-games state from a StateFlow,
 * so observation and mutations behave like the Room-backed implementation.
 */
class FakeFollowedGamesRepository(
    initial: List<FollowedGame> = emptyList()
) : FollowedGamesRepository {

    private val rowsFlow = MutableStateFlow(initial.associateBy { it.gameId })

    var updateReleaseDateCalls = 0
        private set
    var pruneCalls = 0
        private set
    var markNotifiedCalls = 0
        private set

    override fun observeFollowedGameIds(): Flow<Set<Long>> =
        rowsFlow.map { it.keys }

    override suspend fun getAll(): List<FollowedGame> =
        rowsFlow.value.values.toList()

    override suspend fun follow(game: FollowedGame) {
        rowsFlow.value = rowsFlow.value + (game.gameId to game)
    }

    override suspend fun unfollow(gameId: Long): FollowedGame? =
        rowsFlow.value[gameId].also {
            rowsFlow.value = rowsFlow.value - gameId
        }

    override suspend fun markNotified(gameId: Long, offset: ReminderOffset) {
        markNotifiedCalls++
        rowsFlow.value[gameId]?.let { existing ->
            rowsFlow.value = rowsFlow.value + (gameId to existing.copy(
                notifiedOffsets = existing.notifiedOffsets + offset
            ))
        }
    }

    override suspend fun updateReleaseDate(gameId: Long, epochDay: Long) {
        updateReleaseDateCalls++
        rowsFlow.value[gameId]?.let { existing ->
            rowsFlow.value = rowsFlow.value + (gameId to existing.copy(
                releaseEpochDay = epochDay,
                notifiedOffsets = emptySet()
            ))
        }
    }

    override suspend fun pruneReleasedBefore(epochDay: Long) {
        pruneCalls++
        rowsFlow.value = rowsFlow.value.filterValues { it.releaseEpochDay >= epochDay }
    }
}
