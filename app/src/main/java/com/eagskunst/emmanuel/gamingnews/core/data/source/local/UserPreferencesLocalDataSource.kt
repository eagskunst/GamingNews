package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.DEFAULT_REMINDER_HOUR
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesLocalDataSource(context: Context) {

    private val dataStore = context.userPreferencesDataStore

    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = parseThemeMode(prefs[THEME_MODE], prefs[DARK_THEME]),
            dynamicColor = prefs[DYNAMIC_COLOR] ?: true,
            loadImages = prefs[LOAD_IMAGES] ?: true,
            dailyReminder = prefs[DAILY_REMINDER] ?: false,
            dailyReminderHour = prefs[DAILY_REMINDER_HOUR] ?: DEFAULT_REMINDER_HOUR,
            articleOpenMode = parseArticleOpenMode(prefs[ARTICLE_OPEN_MODE])
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE] = mode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateDarkTheme(enabled: Boolean) {
        updateThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    suspend fun updateLoadImages(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[LOAD_IMAGES] = enabled }
    }

    suspend fun updateDailyReminder(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DAILY_REMINDER] = enabled }
    }

    suspend fun updateDailyReminderHour(hour: Int) {
        dataStore.edit { prefs -> prefs[DAILY_REMINDER_HOUR] = hour }
    }

    suspend fun updateArticleOpenMode(mode: ArticleOpenMode) {
        dataStore.edit { prefs -> prefs[ARTICLE_OPEN_MODE] = mode.name }
    }

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val LOAD_IMAGES = booleanPreferencesKey("load_images")
        private val DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
        private val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        private val ARTICLE_OPEN_MODE = stringPreferencesKey("article_open_mode")

        private fun parseThemeMode(themeModeName: String?, legacyDarkTheme: Boolean?): ThemeMode {
            if (themeModeName != null) {
                return ThemeMode.entries.find { it.name == themeModeName } ?: ThemeMode.SYSTEM
            }
            if (legacyDarkTheme != null) {
                return if (legacyDarkTheme) ThemeMode.DARK else ThemeMode.LIGHT
            }
            return ThemeMode.SYSTEM
        }

        private fun parseArticleOpenMode(value: String?): ArticleOpenMode =
            value?.let { name ->
                ArticleOpenMode.entries.find { it.name == name }
            } ?: ArticleOpenMode.EXTERNAL_BROWSER
    }
}

