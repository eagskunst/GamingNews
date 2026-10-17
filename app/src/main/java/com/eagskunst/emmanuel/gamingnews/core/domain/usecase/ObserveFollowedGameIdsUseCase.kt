package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import kotlinx.coroutines.flow.Flow

class ObserveFollowedGameIdsUseCase(private val repository: FollowedGamesRepository) {
    operator fun invoke(): Flow<Set<Long>> = repository.observeFollowedGameIds()
}
