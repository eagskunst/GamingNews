package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReleaseDao {

    @Query("SELECT * FROM releases ORDER BY releaseDate ASC")
    fun observeAll(): Flow<List<ReleaseEntity>>

    @Query("DELETE FROM releases")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(releases: List<ReleaseEntity>)
}
