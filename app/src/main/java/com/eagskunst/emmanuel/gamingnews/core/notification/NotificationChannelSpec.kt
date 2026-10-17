package com.eagskunst.emmanuel.gamingnews.core.notification

import android.app.NotificationManager
import androidx.annotation.StringRes
import com.eagskunst.emmanuel.gamingnews.R

/**
 * Every notification channel the app registers. The daily-reminder channel keeps its original
 * ID so channel-level user settings (importance overrides, blocked state) survive upgrades.
 */
enum class NotificationChannelSpec(
    val channelId: String,
    val importance: Int,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int
) {
    DAILY_REMINDER(
        channelId = "daily_reminder_channel",
        importance = NotificationManager.IMPORTANCE_HIGH,
        nameRes = R.string.notification_channel_daily_reminder_name,
        descriptionRes = R.string.notification_channel_daily_reminder_description
    ),
    GAME_RELEASES(
        channelId = "game_releases",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
        nameRes = R.string.notification_channel_game_releases_name,
        descriptionRes = R.string.notification_channel_game_releases_description
    ),
    NEWS_TOPICS(
        channelId = "news_topics",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
        nameRes = R.string.notification_channel_news_topics_name,
        descriptionRes = R.string.notification_channel_news_topics_description
    )
}
