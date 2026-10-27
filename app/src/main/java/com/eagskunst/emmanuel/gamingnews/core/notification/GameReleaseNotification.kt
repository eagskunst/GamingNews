package com.eagskunst.emmanuel.gamingnews.core.notification

import androidx.annotation.ArrayRes
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset

/**
 * Everything needed to post one game-release notification: stable per-kind identity (for the
 * notification ID) plus the display payload.
 */
class GameReleaseNotification(
    val gameId: Long,
    val name: String,
    val platformsLabel: String,
    val gameUrl: String?,
    val kind: Kind
) {

    sealed interface Kind {
        /** Stable index inside the notification-ID space of a game. */
        val kindIndex: Int

        /** String-array resource of title templates for this kind. */
        @get:ArrayRes
        val titleArrayRes: Int

        data class Offset(val offset: ReminderOffset) : Kind {
            override val kindIndex: Int
                get() = when (offset) {
                    ReminderOffset.ONE_WEEK_BEFORE -> 0
                    ReminderOffset.ONE_DAY_BEFORE -> 1
                    ReminderOffset.RELEASE_DAY -> 2
                }

            override val titleArrayRes: Int
                get() = when (offset) {
                    ReminderOffset.ONE_WEEK_BEFORE -> R.array.notification_release_week_before
                    ReminderOffset.ONE_DAY_BEFORE -> R.array.notification_release_day_before
                    ReminderOffset.RELEASE_DAY -> R.array.notification_release_day
                }
        }
    }

    /** Notification ID: each kind gets its own slot so reminders for one game can coexist. */
    val notificationId: Int get() = (gameId * KIND_SLOTS + kind.kindIndex).toInt()

    private companion object {
        const val KIND_SLOTS = 8L
    }
}
