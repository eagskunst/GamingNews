package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FeedProvidersRepository

class RestoreFeedProvidersDefaultsUseCase(private val repository: FeedProvidersRepository) {
    suspend operator fun invoke() = repository.restoreDefaults()
}
