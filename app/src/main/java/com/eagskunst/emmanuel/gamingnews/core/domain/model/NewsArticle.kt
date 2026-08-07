package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.util.Date

data class NewsArticle(
    val link: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val publicationDate: Date,
    val sourceName: String
)
