package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.notification.ReleaseNotifier
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import com.eagskunst.emmanuel.gamingnews.worker.GameReleaseScheduler
import java.time.LocalDate
import javax.inject.Inject

enum class SyncOutcome {
    /** Sync finished (or there was nothing to do); the worker reports success. */
    Ok,

    /** Fresh dates couldn't be fetched; reminders still fired from stored dates. */
    RetryNetwork
}

/**
 * Daily maintenance pass over followed games:
 *
 * 1. Fetches fresh exact release days from IGDB and silently stores any changed date
 *    (resetting the delivered-reminders bitmask); when a game no longer reports an exact
 *    date the follow is removed. A failed fetch is non-fatal — reminders still fire from
 *    stored dates and the worker retries later.
 * 2. Fires due reminders per follow, earliest-offset first (week → day-before → release day).
 *    A "week before" reminder is skipped when the release is already within a day — that
 *    catch-up would only fire for follows created inside the last week.
 * 3. Prunes follows released more than a week ago, and cancels the periodic check when
 *    nothing is followed anymore.
 */
class SyncFollowedGamesUseCase @Inject constructor(
    private val followedGamesRepository: FollowedGamesRepository,
    private val releasesRepository: ReleasesRepository,
    private val releaseNotifier: ReleaseNotifier,
    private val gameReleaseScheduler: GameReleaseScheduler,
    private val clock: () -> LocalDate = LocalDate::now
) {

    suspend operator fun invoke(): SyncOutcome {
        val follows = followedGamesRepository.getAll()
        if (follows.isEmpty()) return SyncOutcome.Ok

        var outcome = SyncOutcome.Ok
        val remaining = LinkedHashMap<Long, FollowedGame>()
        follows.forEach { remaining[it.gameId] = it }

        val storedDays = remaining.mapValues { it.value.releaseEpochDay }
        val notBeforeEpochSeconds =
            (clock().toEpochDay() - RELEASED_PRUNE_GRACE_DAYS) * SECONDS_PER_DAY
        when (
            val result = releasesRepository.fetchExactReleaseDays(
                storedDays,
                notBeforeEpochSeconds
            )
        ) {
            is Result.Success -> reconcile(result.data, follows, remaining)
            else -> outcome = SyncOutcome.RetryNetwork
        }

        fireDueReminders(remaining.values, clock().toEpochDay())

        followedGamesRepository.pruneReleasedBefore(
            clock().toEpochDay() - RELEASED_PRUNE_GRACE_DAYS
        )

        if (followedGamesRepository.getAll().isEmpty()) {
            gameReleaseScheduler.cancel()
        }

        return outcome
    }

    private suspend fun reconcile(
        freshDays: Map<Long, Long>,
        follows: List<FollowedGame>,
        remaining: MutableMap<Long, FollowedGame>
    ) {
        follows.forEach { game ->
            val freshDay = freshDays[game.gameId]
            when {
                // No exact date on IGDB anymore: drop the follow without notifying —
                // the release card itself loses its precision marker too.
                freshDay == null -> {
                    followedGamesRepository.unfollow(game.gameId)
                    remaining.remove(game.gameId)
                }
                freshDay != game.releaseEpochDay -> {
                    followedGamesRepository.updateReleaseDate(game.gameId, freshDay)
                    remaining[game.gameId] = game.copy(
                        releaseEpochDay = freshDay,
                        notifiedOffsets = emptySet()
                    )
                }
            }
        }
    }

    private suspend fun fireDueReminders(games: Collection<FollowedGame>, today: Long) {
        val canNotify = releaseNotifier.canNotify()
        games.forEach { game ->
            NOTIFY_ORDER.forEach { offset ->
                if (offset !in game.offsets || offset in game.notifiedOffsets) return@forEach
                // Stale catch-up cap: a "week before" reminder is pointless when the game
                // releases today or tomorrow.
                if (offset == ReminderOffset.ONE_WEEK_BEFORE &&
                    game.releaseEpochDay - today <= 1
                ) {
                    return@forEach
                }
                if (game.releaseEpochDay - offset.daysBefore > today) return@forEach
                // Notifications currently blocked (permission revoked, channel off): don't
                // mark, so nothing is lost if the user re-enables them.
                if (!canNotify) return@forEach

                releaseNotifier.notifyOffset(game, offset)
                followedGamesRepository.markNotified(game.gameId, offset)
            }
        }
    }

    private companion object {
        /** Delivered in catch-up order: the furthest-out reminder goes first. */
        val NOTIFY_ORDER = listOf(
            ReminderOffset.ONE_WEEK_BEFORE,
            ReminderOffset.ONE_DAY_BEFORE,
            ReminderOffset.RELEASE_DAY
        )
        const val RELEASED_PRUNE_GRACE_DAYS = 7L
        const val SECONDS_PER_DAY = 86_400L
    }
}
