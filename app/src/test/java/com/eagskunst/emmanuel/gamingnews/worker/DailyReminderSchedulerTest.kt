package com.eagskunst.emmanuel.gamingnews.worker

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DailyReminderSchedulerTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(context)
        } catch (_: IllegalStateException) {
            // already initialized
        }
    }

    @Test
    fun `when scheduling for an hour in the future then initial delay is positive and less than a day`() {
        val current = Calendar.getInstance()
        val futureHour = (current.get(Calendar.HOUR_OF_DAY) + 1) % 24

        val delay = DailyReminderScheduler.computeInitialDelay(futureHour)

        assertTrue(delay > 0)
        assertTrue(delay < TimeUnit.DAYS.toMillis(1))
    }

    @Test
    fun `when scheduling for the current hour then initial delay schedules for tomorrow`() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val delay = DailyReminderScheduler.computeInitialDelay(currentHour)

        assertTrue(delay > 0)
        // Delay should be just under 24 hours because we target the same hour tomorrow.
        assertTrue(delay <= TimeUnit.DAYS.toMillis(1))
    }
}
