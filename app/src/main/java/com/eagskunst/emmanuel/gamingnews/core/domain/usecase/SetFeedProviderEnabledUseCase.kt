package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FeedProvidersRepository

class SetFeedProviderEnabledUseCase(private val repository: FeedProvidersRepository) {
    suspend operator fun invoke(id: String, enabled: Boolean) =
        repository.setProviderEnabled(id, enabled)
}
