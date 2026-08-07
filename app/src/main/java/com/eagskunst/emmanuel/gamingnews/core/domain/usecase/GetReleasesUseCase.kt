package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReleasesRepository
import kotlinx.coroutines.flow.Flow

class GetReleasesUseCase(private val repository: ReleasesRepository) {
    operator fun invoke(forceRefresh: Boolean = false): Flow<Result<List<GameRelease>>> =
        repository.releasesStream(forceRefresh)
}
