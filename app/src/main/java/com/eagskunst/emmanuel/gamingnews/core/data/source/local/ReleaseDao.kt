package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseCoverageEntity
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

    @Query("SELECT * FROM release_coverage WHERE id = ${ReleaseCoverageEntity.COVERAGE_ROW_ID}")
    suspend fun getCoverage(): ReleaseCoverageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCoverage(coverage: ReleaseCoverageEntity)

    /** Atomically replaces the whole release cache and commits the new coverage metadata. */
    @Transaction
    suspend fun replaceCache(releases: List<ReleaseEntity>, coverage: ReleaseCoverageEntity) {
        clear()
        insertAll(releases)
        upsertCoverage(coverage)
    }

    /** Atomically appends a fetched page and commits the updated coverage metadata. */
    @Transaction
    suspend fun commitPage(releases: List<ReleaseEntity>, coverage: ReleaseCoverageEntity) {
        insertAll(releases)
        upsertCoverage(coverage)
    }
}
