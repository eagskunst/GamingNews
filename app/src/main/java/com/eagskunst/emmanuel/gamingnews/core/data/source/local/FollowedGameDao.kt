package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.FollowedGameEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FollowedGameDao {

    @Query("SELECT gameId FROM followed_games")
    fun observeIds(): Flow<List<Long>>

    @Query("SELECT * FROM followed_games")
    suspend fun getAll(): List<FollowedGameEntity>

    @Query("SELECT * FROM followed_games WHERE gameId = :gameId")
    suspend fun get(gameId: Long): FollowedGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(game: FollowedGameEntity)

    @Query("DELETE FROM followed_games WHERE gameId = :gameId")
    suspend fun delete(gameId: Long)

    /** Drops games whose stored release day lies before [epochDay]. */
    @Query("DELETE FROM followed_games WHERE releaseEpochDay < :epochDay")
    suspend fun deleteReleasedBefore(epochDay: Long)

    /** Commits the post-sync state of a follow (a single UPDATE is already atomic). */
    @Query(
        "UPDATE followed_games SET releaseEpochDay = :releaseEpochDay, " +
            "notifiedOffsets = :notifiedOffsets, lastSyncedAt = :lastSyncedAt " +
            "WHERE gameId = :gameId"
    )
    suspend fun updateSync(
        gameId: Long,
        releaseEpochDay: Long,
        notifiedOffsets: Int,
        lastSyncedAt: Date?
    )

    /** ORs [bit] into the delivered-reminders bitmask for the current release date. */
    @Query("UPDATE followed_games SET notifiedOffsets = notifiedOffsets | :bit WHERE gameId = :gameId")
    suspend fun markNotified(gameId: Long, bit: Int)
}
