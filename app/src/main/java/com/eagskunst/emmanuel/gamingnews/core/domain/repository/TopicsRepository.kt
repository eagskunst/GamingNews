package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import kotlinx.coroutines.flow.Flow

interface TopicsRepository {
    fun topicsStream(): Flow<List<Topic>>
    suspend fun addTopic(topic: Topic)
    suspend fun removeTopic(topic: Topic)
}
