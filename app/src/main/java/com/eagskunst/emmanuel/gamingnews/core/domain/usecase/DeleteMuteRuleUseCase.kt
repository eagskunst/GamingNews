package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.MuteRulesRepository

class DeleteMuteRuleUseCase(private val repository: MuteRulesRepository) {
    suspend operator fun invoke(ruleId: String) = repository.deleteRule(ruleId)
}
