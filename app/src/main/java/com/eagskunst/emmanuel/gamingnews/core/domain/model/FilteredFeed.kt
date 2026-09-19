package com.eagskunst.emmanuel.gamingnews.core.domain.model

data class FilteredFeed(
    val articles: List<NewsArticle>,
    val mutedCount: Int,
    val revealedMutedLinks: Set<String>,
    val isRevealed: Boolean,
    val emptyState: EmptyState,
    val sourceLinks: Set<String>,
) {
    enum class EmptyState {
        NONE,
        EMPTY_FEED,
        NO_SEARCH_RESULTS,
        ALL_MUTED
    }
}

data class FilteredReviewFeed(
    val feed: FilteredFeed,
    val failedSources: List<FailedReviewSource>,
)
