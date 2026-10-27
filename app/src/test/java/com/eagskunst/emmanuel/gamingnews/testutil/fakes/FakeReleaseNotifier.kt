package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.notification.ReleaseNotifier

/**
 * Recording [ReleaseNotifier] fake: captures each posted reminder notification in order
 * so tests can assert what the user would have seen.
 */
class FakeReleaseNotifier : ReleaseNotifier {

    /** Set to false to simulate notifications being blocked (permission revoked, etc.). */
    var canNotifyResult = true

    val offsetNotifications = mutableListOf<Pair<Long, ReminderOffset>>()

    override fun canNotify(): Boolean = canNotifyResult

    override fun notifyOffset(game: FollowedGame, offset: ReminderOffset) {
        offsetNotifications += game.gameId to offset
    }
}
