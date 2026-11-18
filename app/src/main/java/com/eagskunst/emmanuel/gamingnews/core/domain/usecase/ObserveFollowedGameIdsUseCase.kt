package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.isReleased
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * IDs of games whose follow can still remind (release day today or later). A released
 * follow stays in the Followed list but must not light the bell / Follow button: the game
 * may have a later launch the user can only re-follow if it reads as "not followed".
 */
class ObserveFollowedGameIdsUseCase(
    private val repository: FollowedGamesRepository,
    private val clock: () -> LocalDate = LocalDate::now
) {
    operator fun invoke(): Flow<Set<Long>> = repository.observeAll().map { games ->
        val today = clock().toEpochDay()
        games.filterNot { it.isReleased(today) }.mapTo(HashSet()) { it.gameId }
    }
}
