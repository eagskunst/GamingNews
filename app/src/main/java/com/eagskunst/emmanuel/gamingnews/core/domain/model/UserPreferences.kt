package com.eagskunst.emmanuel.gamingnews.core.domain.model

const val DEFAULT_REMINDER_HOUR = 9

data class UserPreferences(
    val themeMode: ThemeMode,
    val dynamicColor: Boolean,
    val loadImages: Boolean,
    val dailyReminder: Boolean,
    val dailyReminderHour: Int,
    val articleOpenMode: ArticleOpenMode
)

