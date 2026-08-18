package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateDynamicColor(enabled: Boolean)
    suspend fun updateDarkTheme(enabled: Boolean)
    suspend fun updateLoadImages(enabled: Boolean)
    suspend fun updateDailyReminder(enabled: Boolean)
    suspend fun updateDailyReminderHour(hour: Int)
    suspend fun updateArticleOpenMode(mode: ArticleOpenMode)
}

