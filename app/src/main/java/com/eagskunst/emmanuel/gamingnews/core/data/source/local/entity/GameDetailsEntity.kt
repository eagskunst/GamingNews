package com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached IGDB response for one game detail page. [json] is the serialized
 * `IgdbGameDetailsDto`, so new fields never need a schema change; [fetchedAt] (epoch millis)
 * drives staleness and pruning.
 */
@Entity(tableName = "game_details")
data class GameDetailsEntity(
    @PrimaryKey val gameId: Long,
    val json: String,
    val fetchedAt: Long
)
