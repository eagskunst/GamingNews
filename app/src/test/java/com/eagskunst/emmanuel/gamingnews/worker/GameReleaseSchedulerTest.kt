package com.eagskunst.emmanuel.gamingnews.worker

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFollowedGamesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GameReleaseSchedulerTest {

    private lateinit var context: Application
    private val followedGamesRepository = FakeFollowedGamesRepository()
    private lateinit var scheduler: GameReleaseScheduler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(context)
        } catch (_: IllegalStateException) {
            // already initialized
        }
        scheduler = GameReleaseScheduler(context, followedGamesRepository)
    }

    private suspend fun workState(name: String): WorkInfo.State? =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(name)
            .get()
            .firstOrNull()
            ?.state

    @Test
    fun `given nothing scheduled when ensureScheduled runs then the daily check is enqueued`() = runTest {
        scheduler.ensureScheduled()

        assertEquals(WorkInfo.State.ENQUEUED, workState("game_release_check"))
    }

    @Test
    fun `given follows exist when cancelIfNoneFollowed runs then the work stays scheduled`() = runTest {
        followedGamesRepository.follow(Fixtures.followedGame(gameId = 10))
        scheduler.ensureScheduled()

        scheduler.cancelIfNoneFollowed()

        assertEquals(WorkInfo.State.ENQUEUED, workState("game_release_check"))
    }

    @Test
    fun `given no follows when cancelIfNoneFollowed runs then the work is cancelled`() = runTest {
        scheduler.ensureScheduled()

        scheduler.cancelIfNoneFollowed()

        assertTrue(workState("game_release_check") == null ||
            workState("game_release_check") == WorkInfo.State.CANCELLED)
    }

    @Test
    fun `given a schedule when reschedule runs then the work is re-enqueued`() = runTest {
        scheduler.ensureScheduled()

        scheduler.reschedule()

        assertEquals(WorkInfo.State.ENQUEUED, workState("game_release_check"))
    }
}
