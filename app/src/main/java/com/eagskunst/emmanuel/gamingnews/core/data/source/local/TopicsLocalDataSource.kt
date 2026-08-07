package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.topicsDataStore: DataStore<Preferences> by preferencesDataStore(name = "topics")

class TopicsLocalDataSource(context: Context) {

    private val dataStore = context.topicsDataStore

    val topics: Flow<List<Topic>> = dataStore.data.map { prefs ->
        prefs[TOPICS]?.map { Topic(it) }?.sortedBy { it.name } ?: emptyList()
    }

    suspend fun addTopic(topic: Topic) {
        dataStore.edit { prefs ->
            val current = prefs[TOPICS] ?: emptySet()
            prefs[TOPICS] = current + topic.name
        }
    }

    suspend fun removeTopic(topic: Topic) {
        dataStore.edit { prefs ->
            val current = prefs[TOPICS] ?: emptySet()
            prefs[TOPICS] = current - topic.name
        }
    }

    companion object {
        private val TOPICS = stringSetPreferencesKey("topics")
    }
}
