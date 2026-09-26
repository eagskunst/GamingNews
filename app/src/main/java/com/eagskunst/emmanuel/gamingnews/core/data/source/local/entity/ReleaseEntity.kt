package com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * One row per IGDB release-date record. Platform/date variants of the same game are kept as
 * separate rows so they survive caching and pagination without overwriting each other.
 */
@Entity(tableName = "releases")
data class ReleaseEntity(
    @PrimaryKey val id: Long,
    val gameId: Long,
    val platformId: Int,
    val name: String,
    val coverUrl: String?,
    val releaseDate: Date,
    val gameUrl: String?,
    val fetchedAt: Date = Date()
)

/**
 * Single-row table describing which platform set + query window the release cache covers and
 * whether every relevant page was fetched. The key is derived from supported platform IDs and
 * the window bounds, so label/order changes never invalidate it.
 */
@Entity(tableName = "release_coverage")
data class ReleaseCoverageEntity(
    @PrimaryKey val id: Int = COVERAGE_ROW_ID,
    val coverageKey: String,
    val nextOffset: Int,
    val isComplete: Boolean,
    val updatedAt: Date = Date()
) {
    companion object {
        const val COVERAGE_ROW_ID = 1
    }
}
