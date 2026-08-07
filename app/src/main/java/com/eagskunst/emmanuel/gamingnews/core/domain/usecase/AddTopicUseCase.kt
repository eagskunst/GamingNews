package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.TopicsRepository

class AddTopicUseCase(private val repository: TopicsRepository) {
    suspend operator fun invoke(topic: Topic) = repository.addTopic(topic)
}
