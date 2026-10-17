package com.eagskunst.emmanuel.gamingnews.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SyncFollowedGamesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SyncOutcome
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GameReleaseCheckWorkerTest {

    private val syncFollowedGames: SyncFollowedGamesUseCase = mockk()

    private fun buildWorker(context: Context): GameReleaseCheckWorker =
        TestListenableWorkerBuilder<GameReleaseCheckWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker =
                    GameReleaseCheckWorker(appContext, workerParameters, syncFollowedGames)
            })
            .build()

    @Test
    fun `given a clean sync when doWork runs then it returns success`() = runTest {
        coEvery { syncFollowedGames() } returns SyncOutcome.Ok
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = buildWorker(context).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `given a network failure during sync when doWork runs then it requests a retry`() = runTest {
        coEvery { syncFollowedGames() } returns SyncOutcome.RetryNetwork
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = buildWorker(context).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
