package com.eagskunst.emmanuel.gamingnews.core.domain.notification

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset

/**
 * Domain-facing contract for posting game-release notifications. Implemented in the
 * platform layer so sync logic stays free of Android dependencies.
 */
interface ReleaseNotifier {

    /** Whether posting release reminders would be visible to the user right now. */
    fun canNotify(): Boolean

    /** Posts (or schedules) a reminder for [game] at [offset]. */
    fun notifyOffset(game: FollowedGame, offset: ReminderOffset)
}
