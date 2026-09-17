package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.MuteRulesRepository
import kotlinx.coroutines.flow.Flow

class ObserveMuteRulesUseCase(private val repository: MuteRulesRepository) {
    operator fun invoke(): Flow<List<MuteRule>> = repository.observeRules()
}
