package com.eagskunst.emmanuel.gamingnews.core.notification

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppNotifierTest {

    private lateinit var context: Application
    private lateinit var notifier: AppNotifier
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notifier = AppNotifier(context)
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        shadowOf(notificationManager).setNotificationsEnabled(true)
    }

    @Test
    fun `when ensureChannels is called then all three app channels are registered`() {
        notifier.ensureChannels()

        val channelIds = shadowOf(notificationManager).notificationChannels.map { it.id }
        assertEquals(
            NotificationChannelSpec.entries.map { it.channelId }.sorted(),
            channelIds.sorted()
        )
    }

    @Test
    fun `given all prerequisites when canNotify is checked then it returns true`() {
        notifier.ensureChannels()

        assertTrue(notifier.canNotify(NotificationChannelSpec.GAME_RELEASES))
    }

    @Test
    fun `given permission denied when canNotify is checked then it returns false`() {
        notifier.ensureChannels()
        shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        assertFalse(notifier.canNotify(NotificationChannelSpec.GAME_RELEASES))
    }

    @Test
    fun `given notifications disabled when canNotify is checked then it returns false`() {
        notifier.ensureChannels()
        shadowOf(notificationManager).setNotificationsEnabled(false)

        assertFalse(notifier.canNotify(NotificationChannelSpec.GAME_RELEASES))
    }

    @Test
    fun `given a blocked channel when canNotify is checked then it returns false`() {
        notifier.ensureChannels()
        val blocked = NotificationChannel(
            NotificationChannelSpec.GAME_RELEASES.channelId,
            "Game releases",
            NotificationManager.IMPORTANCE_NONE
        )
        notificationManager.createNotificationChannel(blocked)

        assertFalse(notifier.canNotify(NotificationChannelSpec.GAME_RELEASES))
    }

    @Test
    fun `given a missing channel when canNotify is checked then it returns false`() {
        // The Application registers channels onCreate; delete to simulate a missing channel.
        notificationManager.deleteNotificationChannel(NotificationChannelSpec.GAME_RELEASES.channelId)

        assertFalse(notifier.canNotify(NotificationChannelSpec.GAME_RELEASES))
    }

    @Test
    fun `given a different blocked channel when canNotify is checked then it still returns true`() {
        notifier.ensureChannels()
        val blocked = NotificationChannel(
            NotificationChannelSpec.NEWS_TOPICS.channelId,
            "News topics",
            NotificationManager.IMPORTANCE_NONE
        )
        notificationManager.createNotificationChannel(blocked)

        assertTrue(notifier.canNotify(NotificationChannelSpec.GAME_RELEASES))
    }
}
