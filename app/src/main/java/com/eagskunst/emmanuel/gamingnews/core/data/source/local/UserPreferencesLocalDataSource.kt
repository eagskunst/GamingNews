package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesLocalDataSource(context: Context) {

    private val dataStore = context.userPreferencesDataStore

    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            darkTheme = prefs[DARK_THEME] ?: false,
            loadImages = prefs[LOAD_IMAGES] ?: true,
            dailyReminder = prefs[DAILY_REMINDER] ?: false
        )
    }

    suspend fun updateDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DARK_THEME] = enabled }
    }

    suspend fun updateLoadImages(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[LOAD_IMAGES] = enabled }
    }

    suspend fun updateDailyReminder(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DAILY_REMINDER] = enabled }
    }

    companion object {
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val LOAD_IMAGES = booleanPreferencesKey("load_images")
        private val DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
    }
}
