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

/**
 * Days after release during which a follow is still "active": synced with IGDB and eligible
 * for a late release-day reminder. Older follows only live on in the Followed list.
 */
const val ACTIVE_FOLLOW_GRACE_DAYS = 7L

/** [todayEpochDay] is the device's calendar day compared against the UTC release day. */
fun FollowedGame.isReleased(todayEpochDay: Long): Boolean = releaseEpochDay < todayEpochDay

fun FollowedGame.isActive(todayEpochDay: Long): Boolean =
    releaseEpochDay >= todayEpochDay - ACTIVE_FOLLOW_GRACE_DAYS

/** Followed-list order: upcoming soonest first, then released most recent first. */
fun List<FollowedGame>.sortedForFollowedList(todayEpochDay: Long): List<FollowedGame> {
    val (released, upcoming) = partition { it.isReleased(todayEpochDay) }
    return upcoming.sortedBy { it.releaseEpochDay } +
        released.sortedByDescending { it.releaseEpochDay }
}
