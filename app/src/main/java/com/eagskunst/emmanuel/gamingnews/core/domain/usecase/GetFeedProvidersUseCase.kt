package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FeedProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FeedProvidersRepository
import kotlinx.coroutines.flow.Flow

class GetFeedProvidersUseCase(private val repository: FeedProvidersRepository) {
    operator fun invoke(category: NewsCategory? = null): Flow<List<FeedProvider>> =
        repository.providersStream(category)
}
