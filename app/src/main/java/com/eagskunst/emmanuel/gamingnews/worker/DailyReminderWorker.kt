package com.eagskunst.emmanuel.gamingnews.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eagskunst.emmanuel.gamingnews.core.notification.AppNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notifier: AppNotifier
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notifier.showDailyReminder()
        return Result.success()
    }
}
