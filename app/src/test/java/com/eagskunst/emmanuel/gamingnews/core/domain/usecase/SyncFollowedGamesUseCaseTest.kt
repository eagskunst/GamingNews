package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFollowedGamesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleaseNotifier
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleasesRepository
import com.eagskunst.emmanuel.gamingnews.worker.GameReleaseScheduler
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SyncFollowedGamesUseCaseTest {

    private val followedRepository = FakeFollowedGamesRepository()
    private val releasesRepository = FakeReleasesRepository()
    private val notifier = FakeReleaseNotifier()
    private val scheduler: GameReleaseScheduler = mockk(relaxed = true)

    private val today = LocalDate.of(2025, 11, 10)

    private fun todayDay(): Long = today.toEpochDay()

    private fun useCase() = SyncFollowedGamesUseCase(
        followedGamesRepository = followedRepository,
        releasesRepository = releasesRepository,
        releaseNotifier = notifier,
        gameReleaseScheduler = scheduler,
        clock = { today }
    )

    private fun freshDays(vararg days: Pair<Long, Long>) {
        releasesRepository.exactReleaseDaysResult = Result.Success(days.toMap())
    }

    @Test
    fun `given no follows when syncing then it is a no-op`() = runTest {
        assertEquals(SyncOutcome.Ok, useCase()())
        assertEquals(0, releasesRepository.fetchExactReleaseDaysInvocations)
    }

    @Test
    fun `given a release day reminder due when syncing then it is posted and marked`() = runTest {
        followedRepository.follow(
            Fixtures.followedGame(gameId = 10, releaseEpochDay = todayDay())
        )
        freshDays(10L to todayDay())

        assertEquals(SyncOutcome.Ok, useCase()())

        assertEquals(listOf(10L to ReminderOffset.RELEASE_DAY), notifier.offsetNotifications)
        val stored = followedRepository.getAll().single()
        assertEquals(setOf(ReminderOffset.RELEASE_DAY), stored.notifiedOffsets)
    }

    @Test
    fun `given a reminder not yet due when syncing then nothing is posted`() = runTest {
        followedRepository.follow(
            Fixtures.followedGame(
                gameId = 10,
                releaseEpochDay = todayDay() + 3,
                offsets = setOf(ReminderOffset.RELEASE_DAY)
            )
        )
        freshDays(10L to todayDay() + 3)

        useCase()()

        assertTrue(notifier.offsetNotifications.isEmpty())
        assertTrue(followedRepository.getAll().single().notifiedOffsets.isEmpty())
    }

    @Test
    fun `given a reminder window already passed when syncing then catch-up semantics fire it`() = runTest {
        // Release in 3 days but the "week before" moment is in the past: fire now.
        followedRepository.follow(
            Fixtures.followedGame(
                gameId = 10,
                releaseEpochDay = todayDay() + 3,
                offsets = setOf(ReminderOffset.ONE_WEEK_BEFORE)
            )
        )
        freshDays(10L to todayDay() + 3)

        useCase()()

        assertEquals(listOf(10L to ReminderOffset.ONE_WEEK_BEFORE), notifier.offsetNotifications)
    }

    @Test
    fun `given a stale week reminder when the release is imminent then it is skipped`() = runTest {
        // Release tomorrow: a "week before" catch-up would be pointless.
        followedRepository.follow(
            Fixtures.followedGame(
                gameId = 10,
                releaseEpochDay = todayDay() + 1,
                offsets = setOf(ReminderOffset.ONE_WEEK_BEFORE, ReminderOffset.ONE_DAY_BEFORE)
            )
        )
        freshDays(10L to todayDay() + 1)

        useCase()()

        assertEquals(listOf(10L to ReminderOffset.ONE_DAY_BEFORE), notifier.offsetNotifications)
    }

    @Test
    fun `given multiple selected offsets when syncing then they fire furthest out first`() = runTest {
        followedRepository.follow(
            Fixtures.followedGame(
                gameId = 10,
                releaseEpochDay = todayDay(),
                offsets = ReminderOffset.entries.toSet()
            )
        )
        freshDays(10L to todayDay())

        useCase()()

        // Week reminder is skipped by the stale catch-up cap; day-before then release day.
        assertEquals(
            listOf(10L to ReminderOffset.ONE_DAY_BEFORE, 10L to ReminderOffset.RELEASE_DAY),
            notifier.offsetNotifications
        )
    }

    @Test
    fun `given a slipped date when syncing then it is stored silently and the mask resets`() = runTest {
        followedRepository.follow(
            Fixtures.followedGame(
                gameId = 10,
                releaseEpochDay = todayDay() + 5,
                offsets = setOf(ReminderOffset.RELEASE_DAY),
                notifiedOffsets = setOf(ReminderOffset.RELEASE_DAY)
            )
        )
        freshDays(10L to todayDay() + 10)

        useCase()()

        assertTrue(notifier.offsetNotifications.isEmpty())
        assertEquals(1, followedRepository.updateReleaseDateCalls)
        val stored = followedRepository.getAll().single()
        assertEquals(todayDay() + 10, stored.releaseEpochDay)
        assertTrue(stored.notifiedOffsets.isEmpty())
    }

    @Test
    fun `given follows when syncing then the fetch is bounded by stored days and the prune horizon`() = runTest {
        followedRepository.follow(Fixtures.followedGame(gameId = 10, releaseEpochDay = todayDay() + 5))
        freshDays(10L to todayDay() + 5)

        useCase()()

        assertEquals(mapOf(10L to todayDay() + 5), releasesRepository.lastFetchStoredDays)
        assertEquals((todayDay() - 7) * 86_400L, releasesRepository.lastFetchNotBeforeEpochSeconds)
    }

    @Test
    fun `given a game without an exact date anymore when syncing then the follow is removed silently`() = runTest {
        followedRepository.follow(Fixtures.followedGame(gameId = 10, releaseEpochDay = todayDay() + 5))
        followedRepository.follow(Fixtures.followedGame(gameId = 20, releaseEpochDay = todayDay() + 5))
        freshDays(20L to todayDay() + 5) // game 10 lost its date; 20 unchanged

        useCase()()

        assertEquals(listOf(20L), followedRepository.getAll().map { it.gameId })
        assertTrue(notifier.offsetNotifications.isEmpty())
    }

    @Test
    fun `given the last follow is removed when syncing then the daily check is cancelled`() = runTest {
        followedRepository.follow(Fixtures.followedGame(gameId = 10, releaseEpochDay = todayDay() + 5))
        freshDays() // no exact dates at all

        useCase()()

        assertTrue(followedRepository.getAll().isEmpty())
        verify { scheduler.cancel() }
    }

    @Test
    fun `given a fetch failure when syncing then reminders still fire and a retry is requested`() = runTest {
        followedRepository.follow(Fixtures.followedGame(gameId = 10, releaseEpochDay = todayDay()))
        releasesRepository.exactReleaseDaysResult = Result.Error(RuntimeException("offline"))

        assertEquals(SyncOutcome.RetryNetwork, useCase()())

        // Stored dates still drive reminders; nothing is removed on a failed fetch.
        assertEquals(listOf(10L to ReminderOffset.RELEASE_DAY), notifier.offsetNotifications)
        assertEquals(listOf(10L), followedRepository.getAll().map { it.gameId })
    }

    @Test
    fun `given a game released over a week ago when syncing then it is pruned`() = runTest {
        followedRepository.follow(Fixtures.followedGame(gameId = 10, releaseEpochDay = todayDay() - 8))
        followedRepository.follow(Fixtures.followedGame(gameId = 20, releaseEpochDay = todayDay() - 6))
        freshDays(10L to todayDay() - 8, 20L to todayDay() - 6)

        useCase()()

        assertEquals(listOf(20L), followedRepository.getAll().map { it.gameId })
    }

    @Test
    fun `given a run already delivered reminders when syncing again then nothing is reposted`() = runTest {
        followedRepository.follow(Fixtures.followedGame(gameId = 10, releaseEpochDay = todayDay()))
        freshDays(10L to todayDay())
        val sync = useCase()

        sync()
        sync()

        assertEquals(listOf(10L to ReminderOffset.RELEASE_DAY), notifier.offsetNotifications)
        assertEquals(1, followedRepository.markNotifiedCalls)
    }

    @Test
    fun `given notifications blocked when syncing then nothing posts and bits stay unset`() = runTest {
        notifier.canNotifyResult = false
        followedRepository.follow(Fixtures.followedGame(gameId = 10, releaseEpochDay = todayDay()))
        freshDays(10L to todayDay())

        useCase()()

        assertTrue(notifier.offsetNotifications.isEmpty())
        assertTrue(followedRepository.getAll().single().notifiedOffsets.isEmpty())
        assertEquals(0, followedRepository.markNotifiedCalls)
    }
}
