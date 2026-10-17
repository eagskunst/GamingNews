package com.eagskunst.emmanuel.gamingnews.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SyncFollowedGamesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SyncOutcome
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Daily pass that syncs followed games' release dates with IGDB and fires due reminders.
 * A failed date fetch maps to [Result.retry] so WorkManager re-runs it with backoff while
 * the stored dates keep reminders flowing meanwhile.
 */
@HiltWorker
class GameReleaseCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncFollowedGames: SyncFollowedGamesUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (syncFollowedGames()) {
        SyncOutcome.Ok -> Result.success()
        SyncOutcome.RetryNetwork -> Result.retry()
    }
}
