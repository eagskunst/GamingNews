package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.feedProvidersDataStore: DataStore<Preferences> by preferencesDataStore(name = "feed_providers")

class FeedProvidersLocalDataSource(context: Context) {

    private val dataStore = context.feedProvidersDataStore

    val disabledProviderIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[DISABLED_PROVIDER_IDS] ?: emptySet()
    }

    suspend fun setProviderEnabled(id: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[DISABLED_PROVIDER_IDS] ?: emptySet()
            prefs[DISABLED_PROVIDER_IDS] = if (enabled) current - id else current + id
        }
    }

    suspend fun restoreDefaults() {
        dataStore.edit { prefs ->
            prefs.remove(DISABLED_PROVIDER_IDS)
        }
    }

    companion object {
        private val DISABLED_PROVIDER_IDS = stringSetPreferencesKey("disabled_provider_ids")
    }
}
