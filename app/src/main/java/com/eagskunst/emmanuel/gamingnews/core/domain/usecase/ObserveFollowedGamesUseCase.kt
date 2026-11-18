package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FollowedGamesRepository
import kotlinx.coroutines.flow.Flow

class ObserveFollowedGamesUseCase(private val repository: FollowedGamesRepository) {
    operator fun invoke(): Flow<List<FollowedGame>> = repository.observeAll()
}
