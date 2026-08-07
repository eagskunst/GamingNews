package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.TopicsLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.FirebaseTopicsDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.TopicsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultTopicsRepository @Inject constructor(
    private val localDataSource: TopicsLocalDataSource,
    private val firebaseDataSource: FirebaseTopicsDataSource
) : TopicsRepository {

    override fun topicsStream(): Flow<List<Topic>> = localDataSource.topics

    override suspend fun addTopic(topic: Topic) {
        val normalized = normalizeTopic(topic.name)
        localDataSource.addTopic(Topic(normalized))
        firebaseDataSource.subscribeToTopic(normalized)
    }

    override suspend fun removeTopic(topic: Topic) {
        val normalized = normalizeTopic(topic.name)
        localDataSource.removeTopic(Topic(normalized))
        firebaseDataSource.unsubscribeFromTopic(normalized)
    }

    private fun normalizeTopic(name: String): String {
        return name.trim().replace(" ", "_").uppercase()
    }
}
