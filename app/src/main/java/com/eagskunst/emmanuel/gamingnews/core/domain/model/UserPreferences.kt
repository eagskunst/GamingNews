package com.eagskunst.emmanuel.gamingnews.core.domain.model

data class UserPreferences(
    val themeMode: ThemeMode,
    val dynamicColor: Boolean,
    val loadImages: Boolean,
    val dailyReminder: Boolean,
    val articleOpenMode: ArticleOpenMode
)

