package com.eagskunst.emmanuel.gamingnews.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_TIMEZONE_CHANGED
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.worker.DailyReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimeZoneChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var reminderScheduler: DailyReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TIMEZONE_CHANGED) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                reschedule(reminderScheduler, userPreferencesRepository.userPreferences.first())
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun reschedule(scheduler: DailyReminderScheduler, preferences: UserPreferences) {
            if (preferences.dailyReminder) {
                scheduler.schedule(preferences.dailyReminderHour)
            } else {
                scheduler.cancel()
            }
        }
    }
}
