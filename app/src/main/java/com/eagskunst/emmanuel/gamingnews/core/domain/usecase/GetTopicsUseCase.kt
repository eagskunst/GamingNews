package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.TopicsRepository
import kotlinx.coroutines.flow.Flow

class GetTopicsUseCase(private val repository: TopicsRepository) {
    operator fun invoke(): Flow<List<Topic>> = repository.topicsStream()
}
