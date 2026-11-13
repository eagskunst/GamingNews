package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.GameDetailsEntity

@Dao
interface GameDetailsDao {

    @Query("SELECT * FROM game_details WHERE gameId = :gameId")
    suspend fun get(gameId: Long): GameDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(details: GameDetailsEntity)

    /** Drops cache rows fetched before [epochMillis] so the table stays bounded. */
    @Query("DELETE FROM game_details WHERE fetchedAt < :epochMillis")
    suspend fun deleteFetchedBefore(epochMillis: Long)
}
