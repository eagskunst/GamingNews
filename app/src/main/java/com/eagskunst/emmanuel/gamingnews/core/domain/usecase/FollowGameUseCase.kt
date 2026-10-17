package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import com.eagskunst.emmanuel.gamingnews.worker.GameReleaseScheduler
import java.util.Date

private const val MILLIS_PER_DAY = 86_400_000L

/** Signals that a follow was attempted on a release without a day-precision date. */
class InexactReleaseDateException : IllegalStateException(
    "Only releases with an exact date can be followed"
)

/**
 * Persists a follow for [release] with the chosen [offsets], then makes sure the daily
 * release check is scheduled. Approximate releases (TBA / year / quarter) are rejected
 * because they have no concrete day to remind against.
 */
class FollowGameUseCase(
    private val repository: FollowedGamesRepository,
    private val scheduler: GameReleaseScheduler
) {

    suspend operator fun invoke(
        release: GameRelease,
        offsets: Set<ReminderOffset>
    ): Result<Unit> {
        if (!release.hasExactDate) {
            return Result.Error(InexactReleaseDateException())
        }
        if (offsets.isEmpty()) {
            return Result.Error(IllegalArgumentException("At least one reminder must be selected"))
        }
        restore(
            FollowedGame(
                gameId = release.id,
                name = release.name,
                coverUrl = release.coverUrl,
                gameUrl = release.gameUrl,
                releaseEpochDay = Math.floorDiv(release.releaseDate.time, MILLIS_PER_DAY),
                offsets = offsets,
                notifiedOffsets = emptySet(),
                platformsLabel = release.platforms.joinToString(" · "),
                followedAt = Date(),
                lastSyncedAt = null
            )
        )
        return Result.Success(Unit)
    }

    /** Re-follows a previously removed game (Undo) without re-validating precision. */
    suspend fun restore(game: FollowedGame) {
        repository.follow(game)
        scheduler.ensureScheduled()
    }
}
