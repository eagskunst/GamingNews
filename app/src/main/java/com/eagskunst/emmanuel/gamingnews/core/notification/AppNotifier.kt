package com.eagskunst.emmanuel.gamingnews.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.notification.ReleaseNotifier
import com.eagskunst.emmanuel.gamingnews.views.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for app-owned notifications: channel registration, permission/channel
 * state checks and posting. Release reminders respect Do-Not-Disturb — the
 * [NotificationChannelSpec.GAME_RELEASES] channel uses default importance and
 * [NotificationCompat.CATEGORY_EVENT], so there is no bypass.
 */
@Singleton
class AppNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : ReleaseNotifier {

    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /** Registers every app channel; safe to call repeatedly (updates are idempotent). */
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        NotificationChannelSpec.entries.forEach { spec ->
            val channel = NotificationChannel(
                spec.channelId,
                context.getString(spec.nameRes),
                spec.importance
            ).apply {
                description = context.getString(spec.descriptionRes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Whether a notification on [channel] would actually reach the user: runtime permission
     * granted (API 33+), app notifications not turned off, and the channel itself not blocked.
     */
    fun canNotify(channel: NotificationChannelSpec): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        if (!notificationManager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val androidChannel = notificationManager.getNotificationChannel(channel.channelId)
                ?: return false
            if (androidChannel.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        return true
    }

    fun showDailyReminder() {
        val notification = NotificationCompat
            .Builder(context, NotificationChannelSpec.DAILY_REMINDER.channelId)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.daily_reminder_notification_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppPendingIntent(DAILY_REMINDER_NOTIFICATION_ID))
            .setAutoCancel(true)
            .build()
        notificationManager.notify(DAILY_REMINDER_NOTIFICATION_ID, notification)
    }

    override fun canNotify(): Boolean = canNotify(NotificationChannelSpec.GAME_RELEASES)

    override fun notifyOffset(game: FollowedGame, offset: ReminderOffset) {
        showGameRelease(
            GameReleaseNotification(
                gameId = game.gameId,
                name = game.name,
                platformsLabel = game.platformsLabel,
                gameUrl = game.gameUrl,
                kind = GameReleaseNotification.Kind.Offset(offset)
            )
        )
    }

    fun showGameRelease(notification: GameReleaseNotification) {
        val title = buildTitle(notification)
        val releaseNotification = NotificationCompat
            .Builder(context, NotificationChannelSpec.GAME_RELEASES.channelId)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setContentTitle(title)
            .setContentText(notification.platformsLabel)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(contentIntent(notification))
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notification.notificationId, releaseNotification)
    }

    /** Picks a random localized title template for the notification kind. */
    private fun buildTitle(notification: GameReleaseNotification): String {
        val templates = context.resources.getStringArray(notification.kind.titleArrayRes)
        val template = templates.random()
        return String.format(Locale.getDefault(), template, notification.name)
    }

    private fun contentIntent(notification: GameReleaseNotification): PendingIntent {
        val intent = notification.gameUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { url ->
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ?: Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_RELEASES, true)
        return PendingIntent.getActivity(
            context,
            notification.notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun openAppPendingIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private companion object {
        const val DAILY_REMINDER_NOTIFICATION_ID = 1
    }
}
