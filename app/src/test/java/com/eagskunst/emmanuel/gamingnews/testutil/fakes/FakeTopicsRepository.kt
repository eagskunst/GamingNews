package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.TopicsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Reusable [TopicsRepository] fake backed by a [MutableStateFlow], shared across all use
 * case/ViewModel/Compose UI tests that depend on topics.
 */
class FakeTopicsRepository(
    initial: List<Topic> = emptyList()
) : TopicsRepository {

    val topicsFlow = MutableStateFlow(initial)

    override fun topicsStream(): Flow<List<Topic>> = topicsFlow

    override suspend fun addTopic(topic: Topic) {
        topicsFlow.update { it + topic }
    }

    override suspend fun removeTopic(topic: Topic) {
        topicsFlow.update { it - topic }
    }
}
