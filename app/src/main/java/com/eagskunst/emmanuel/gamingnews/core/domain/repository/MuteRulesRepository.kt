package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import kotlinx.coroutines.flow.Flow

interface MuteRulesRepository {
    fun observeRules(): Flow<List<MuteRule>>
    suspend fun upsertRule(rule: MuteRule)
    suspend fun deleteRule(ruleId: String)
}

/** Thrown when a write would create a rule identical to an existing one. */
class DuplicateMuteRuleException : IllegalStateException("An identical mute rule already exists")
