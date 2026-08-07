package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.igdbAuthDataStore: DataStore<Preferences> by preferencesDataStore(name = "igdb_auth")

class IgdbAuthLocalDataSource(private val context: Context) {

    private val dataStore = context.igdbAuthDataStore

    suspend fun getAccessToken(): String? = dataStore.data
        .map { prefs ->
            val token = prefs[ACCESS_TOKEN]
            val expiresAt = prefs[EXPIRES_AT] ?: 0L
            if (token.isNullOrBlank() || System.currentTimeMillis() >= expiresAt) {
                null
            } else {
                token
            }
        }
        .first()

    suspend fun saveAccessToken(token: String, expiresIn: Long) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = token
            prefs[EXPIRES_AT] = System.currentTimeMillis() + (expiresIn * 1000L)
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("igdb_access_token")
        private val EXPIRES_AT = longPreferencesKey("igdb_token_expires_at")
    }
}
