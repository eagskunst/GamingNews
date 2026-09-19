package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FeedProvidersRepository
import kotlinx.coroutines.flow.first

class GetFeedUrlsUseCase(
    private val feedProvidersRepository: FeedProvidersRepository
) {
    suspend operator fun invoke(category: NewsCategory): List<String> =
        feedProvidersRepository.providersStream(category)
            .first()
            .filter { it.enabled }
            .map { it.url }
}
