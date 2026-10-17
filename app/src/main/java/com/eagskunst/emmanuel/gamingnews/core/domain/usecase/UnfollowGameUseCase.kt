package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import com.eagskunst.emmanuel.gamingnews.worker.GameReleaseScheduler

/**
 * Removes a follow and tears down the daily release check when nothing is followed anymore.
 * Returns the removed game so the caller can offer an Undo.
 */
class UnfollowGameUseCase(
    private val repository: FollowedGamesRepository,
    private val scheduler: GameReleaseScheduler
) {
    suspend operator fun invoke(gameId: Long): FollowedGame? {
        val removed = repository.unfollow(gameId)
        scheduler.cancelIfNoneFollowed()
        return removed
    }
}
