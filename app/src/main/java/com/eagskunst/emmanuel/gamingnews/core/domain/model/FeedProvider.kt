package com.eagskunst.emmanuel.gamingnews.core.domain.model

data class FeedProvider(
    val id: String,
    val name: String,
    val url: String,
    val category: NewsCategory,
    val enabled: Boolean = true
)
