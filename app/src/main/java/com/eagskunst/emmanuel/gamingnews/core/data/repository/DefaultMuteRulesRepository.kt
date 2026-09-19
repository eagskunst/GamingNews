package com.eagskunst.emmanuel.gamingnews.core.data.repository

import androidx.room.withTransaction
import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toDomain
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toEntity
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toTabEntities
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.AppDatabase
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.MuteRuleDao
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleDraft
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleValidator
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteScope
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.DuplicateMuteRuleException
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.MuteRulesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMuteRulesRepository @Inject constructor(
    private val database: AppDatabase,
    private val muteRuleDao: MuteRuleDao,
    private val dispatchers: DispatcherProvider
) : MuteRulesRepository {

    override fun observeRules(): Flow<List<MuteRule>> =
        muteRuleDao.observeRules()
            .map { rules -> rules.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun upsertRule(rule: MuteRule) = withContext(dispatchers.io) {
        database.withTransaction {
            val existing = muteRuleDao.getRules().map { it.toDomain() }
            val draft = MuteRuleDraft(
                id = rule.id,
                text = rule.text,
                matchMode = rule.matchMode,
                caseSensitive = rule.caseSensitive,
                scopeEverywhere = rule.scope is MuteScope.Everywhere,
                selectedTabs = (rule.scope as? MuteScope.SelectedTabs)?.tabs.orEmpty()
            )
            if (existing.any { MuteRuleValidator.isDuplicate(draft, it) }) {
                throw DuplicateMuteRuleException()
            }
            muteRuleDao.deleteTabs(rule.id)
            muteRuleDao.insertRule(rule.toEntity())
            muteRuleDao.insertTabs(rule.toTabEntities())
        }
    }

    override suspend fun deleteRule(ruleId: String) = withContext(dispatchers.io) {
        muteRuleDao.deleteRule(ruleId)
    }
}
