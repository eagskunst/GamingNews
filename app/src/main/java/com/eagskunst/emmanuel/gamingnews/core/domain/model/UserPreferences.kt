package com.eagskunst.emmanuel.gamingnews.core.domain.model

data class UserPreferences(
    val darkTheme: Boolean,
    val loadImages: Boolean,
    val dailyReminder: Boolean,
    val articleOpenMode: ArticleOpenMode
)
