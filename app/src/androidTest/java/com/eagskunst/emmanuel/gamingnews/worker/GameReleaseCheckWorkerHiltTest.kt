package com.eagskunst.emmanuel.gamingnews.worker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Runs [GameReleaseCheckWorker] through the app's real WorkManager configuration — i.e. the
 * injected `HiltWorkerFactory`. The unit test suite builds the worker manually, which misses
 * a broken factory wiring (e.g. a missing `androidx.hilt:hilt-compiler` leaves the assisted-
 * factory map empty and every scheduled run fails with "Could not create Worker"). This test
 * guards that path.
 */
@RunWith(AndroidJUnit4::class)
class GameReleaseCheckWorkerHiltTest {

    @Test
    fun when_the_release_check_runs_then_the_hilt_factory_instantiates_the_worker() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<GameReleaseCheckWorker>().build()
        workManager.enqueue(request).result.get(10, TimeUnit.SECONDS)

        // An unconstrained one-time request in a live process is picked up by the greedy
        // scheduler almost immediately; poll until a run has executed AND finished —
        // doWork is suspend, so RUNNING must drain before the result is meaningful.
        val deadline = System.currentTimeMillis() + 60_000
        var info = workManager.getWorkInfoById(request.id).get(10, TimeUnit.SECONDS)
        while (System.currentTimeMillis() < deadline) {
            if (info != null && info.runAttemptCount > 0 && info.state != WorkInfo.State.RUNNING) {
                break
            }
            Thread.sleep(500)
            info = workManager.getWorkInfoById(request.id).get(10, TimeUnit.SECONDS)
        }

        assertTrue("worker never ran", (info?.runAttemptCount ?: 0) > 0)
        // Worker-creation failure surfaces as FAILED; a flaky IGDB fetch surfaces as a
        // retry (ENQUEUED), which still proves the worker was built and ran.
        assertNotEquals(WorkInfo.State.FAILED, info?.state)
    }
}
