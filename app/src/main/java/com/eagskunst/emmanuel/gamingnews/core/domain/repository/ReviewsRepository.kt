package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewFeedSnapshot
import kotlinx.coroutines.flow.Flow

interface ReviewsRepository {
    fun reviewsStream(forceRefresh: Boolean = false): Flow<Result<ReviewFeedSnapshot>>
}
