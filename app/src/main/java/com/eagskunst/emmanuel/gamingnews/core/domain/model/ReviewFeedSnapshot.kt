package com.eagskunst.emmanuel.gamingnews.core.domain.model

data class ReviewFeedSnapshot(
    val articles: List<NewsArticle>,
    val failedSources: List<FailedReviewSource> = emptyList()
)

data class FailedReviewSource(
    val id: String,
    val name: String,
    val reason: FailureReason
) {
    sealed interface FailureReason {
        data class NetworkError(val exception: Throwable) : FailureReason
        data object UnsupportedEligibilityMetadata : FailureReason
    }

    fun toException(): Exception = when (reason) {
        is FailureReason.NetworkError -> reason.exception as? Exception ?: RuntimeException("Network error")
        FailureReason.UnsupportedEligibilityMetadata -> UnsupportedOperationException(
            "$name does not provide reliable game-review eligibility metadata"
        )
    }
}
