package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewFeedSnapshot
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReviewsRepository
import kotlinx.coroutines.flow.Flow

class GetReviewsUseCase(private val repository: ReviewsRepository) {
    operator fun invoke(forceRefresh: Boolean = false): Flow<Result<ReviewFeedSnapshot>> =
        repository.reviewsStream(forceRefresh)
}
