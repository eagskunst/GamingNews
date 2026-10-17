package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.util.Date

/**
 * A game the user asked to be reminded about. [releaseEpochDay] is the UTC calendar day of
 * release (IGDB dates are midnight-UTC timestamps); [notifiedOffsets] tracks which reminders
 * were already delivered for the current stored date so a re-run never double-posts.
 */
data class FollowedGame(
    val gameId: Long,
    val name: String,
    val coverUrl: String?,
    val gameUrl: String?,
    val releaseEpochDay: Long,
    val offsets: Set<ReminderOffset>,
    val notifiedOffsets: Set<ReminderOffset>,
    val platformsLabel: String,
    val followedAt: Date,
    val lastSyncedAt: Date?
)
