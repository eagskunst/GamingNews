package com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * A game the user follows for release reminders. One row per game; [releaseEpochDay] is the
 * UTC calendar day IGDB reports, and [offsets]/[notifiedOffsets] are [ReminderOffset] bitmasks
 * — the second tracks which reminders already fired for the current stored date.
 */
@Entity(tableName = "followed_games")
data class FollowedGameEntity(
    @PrimaryKey val gameId: Long,
    val name: String,
    val coverUrl: String?,
    val gameUrl: String?,
    val releaseEpochDay: Long,
    val offsets: Int,
    val notifiedOffsets: Int,
    /** Display label ("PC · PS5") captured at follow time for notification bodies. */
    val platformsLabel: String,
    val followedAt: Date = Date(),
    val lastSyncedAt: Date? = null
)
