package com.eagskunst.emmanuel.gamingnews.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the unique periodic [GameReleaseCheckWorker]: a daily 09:00-local check that runs only
 * while at least one game is followed. Enqueue policies: KEEP when a follow is added (an
 * existing schedule must not be disturbed), REPLACE when the schedule itself has to be
 * re-anchored — e.g. after a timezone change (UPDATE would preserve the stale delay).
 */
@Singleton
class GameReleaseScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val followedGamesRepository: FollowedGamesRepository
) {

    /** Enqueues the daily check if it isn't scheduled yet; a no-op otherwise. */
    fun ensureScheduled() {
        enqueue(ExistingPeriodicWorkPolicy.KEEP)
    }

    /** Re-anchors the daily check to the next 09:00 in the (possibly new) local time zone. */
    fun reschedule() {
        enqueue(ExistingPeriodicWorkPolicy.REPLACE)
    }

    /** Cancels the check when no followed games remain; leaves it running otherwise. */
    suspend fun cancelIfNoneFollowed() {
        if (followedGamesRepository.getAll().isEmpty()) cancel()
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun enqueue(policy: ExistingPeriodicWorkPolicy) {
        val request = PeriodicWorkRequestBuilder<GameReleaseCheckWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(computeInitialDelayToHour(CHECK_HOUR), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }

    private companion object {
        private const val WORK_NAME = "game_release_check"
        private const val CHECK_HOUR = 9
    }
}
