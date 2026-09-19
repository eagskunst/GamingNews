package com.eagskunst.emmanuel.gamingnews.core.domain.model

sealed interface ReviewEligibilityPolicy {
    /**
     * Trusts that every item in a dedicated gaming-reviews feed represents a game review.
     * Used when the publisher's review feed is scoped to games.
     */
    data object GamingFeedReviews : ReviewEligibilityPolicy

    /**
     * Declares that the feed mixes content types and provides no trustworthy per-item
     * game signal, so no item can be positively identified as a game review.
     */
    data object UnsupportedMixedContent : ReviewEligibilityPolicy
}
