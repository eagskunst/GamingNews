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

class IgdbAuthLocalDataSource(
    private val context: Context,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {

    private val dataStore = context.igdbAuthDataStore

    suspend fun getAccessToken(clientId: String): String? = dataStore.data
        .map { prefs ->
            val token = prefs[ACCESS_TOKEN]
            val expiresAt = prefs[EXPIRES_AT] ?: 0L
            val cachedClientId = prefs[CLIENT_ID]
            if (
                token.isNullOrBlank() ||
                cachedClientId != clientId ||
                currentTimeMillis() >= expiresAt - EXPIRATION_SAFETY_MARGIN_MILLIS
            ) {
                null
            } else {
                token
            }
        }
        .first()

    suspend fun saveAccessToken(token: String, expiresIn: Long, clientId: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = token
            prefs[EXPIRES_AT] = currentTimeMillis() + (expiresIn * 1000L)
            prefs[CLIENT_ID] = clientId
        }
    }

    suspend fun invalidateAccessToken(rejectedToken: String, clientId: String) {
        dataStore.edit { prefs ->
            if (prefs[ACCESS_TOKEN] == rejectedToken && prefs[CLIENT_ID] == clientId) {
                prefs.clear()
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private const val EXPIRATION_SAFETY_MARGIN_MILLIS = 60_000L
        private val ACCESS_TOKEN = stringPreferencesKey("igdb_access_token")
        private val EXPIRES_AT = longPreferencesKey("igdb_token_expires_at")
        private val CLIENT_ID = stringPreferencesKey("igdb_token_client_id")
    }
}
