package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewFeedSnapshot
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReviewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Reusable [ReviewsRepository] fake backed by a [MutableStateFlow], shared across
 * Reviews ViewModel/Compose UI tests.
 */
class FakeReviewsRepository(
    initialResult: Result<ReviewFeedSnapshot> = Result.Success(ReviewFeedSnapshot(emptyList()))
) : ReviewsRepository {

    val reviewsResultFlow = MutableStateFlow(initialResult)

    var lastForceRefresh: Boolean? = null
        private set

    override fun reviewsStream(forceRefresh: Boolean): Flow<Result<ReviewFeedSnapshot>> {
        lastForceRefresh = forceRefresh
        return flow { emit(reviewsResultFlow.value) }
    }
}
