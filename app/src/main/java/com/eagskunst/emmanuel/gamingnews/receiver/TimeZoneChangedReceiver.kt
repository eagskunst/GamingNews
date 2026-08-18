package com.eagskunst.emmanuel.gamingnews.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_TIMEZONE_CHANGED
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.worker.DailyReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class TimeZoneChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TIMEZONE_CHANGED) return

        runBlocking {
            reschedule(context, userPreferencesRepository.userPreferences.first())
        }
    }

    companion object {
        fun reschedule(context: Context, preferences: UserPreferences) {
            if (preferences.dailyReminder) {
                DailyReminderScheduler.schedule(context, preferences.dailyReminderHour)
            } else {
                DailyReminderScheduler.cancel(context)
            }
        }
    }
}
