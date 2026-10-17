package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import kotlinx.coroutines.flow.Flow

interface FollowedGamesRepository {

    /** IDs of every followed game, kept hot while observed. */
    fun observeFollowedGameIds(): Flow<Set<Long>>

    suspend fun getAll(): List<FollowedGame>

    /** Inserts or replaces a follow. */
    suspend fun follow(game: FollowedGame)

    /** Removes the follow; returns the removed row so callers can offer an Undo. */
    suspend fun unfollow(gameId: Long): FollowedGame?

    /** Marks [offset] as delivered for the game's current stored release date. */
    suspend fun markNotified(gameId: Long, offset: ReminderOffset)

    /** Stores a new release day and resets the delivered-reminders bitmask. */
    suspend fun updateReleaseDate(gameId: Long, epochDay: Long)

    /** Drops follows whose release day lies before [epochDay]. */
    suspend fun pruneReleasedBefore(epochDay: Long)
}
