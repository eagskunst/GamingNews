package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.MuteRulesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Reusable [MuteRulesRepository] fake backed by a [MutableStateFlow], shared across
 * use case/ViewModel/Compose UI tests that depend on mute rules.
 *
 * [upsertRule] applies the same duplicate policy as the real repository when
 * [enforceDuplicates] is true, so tests can exercise in-transaction duplicate checks.
 */
class FakeMuteRulesRepository(
    initialRules: List<MuteRule> = emptyList()
) : MuteRulesRepository {

    val rulesFlow = MutableStateFlow(initialRules)

    /** When set, writes throw this error instead of mutating [rulesFlow]. */
    var writeError: Exception? = null

    /** When set, [observeRules] fails with this error instead of emitting [rulesFlow]. */
    var rulesError: Exception? = null
    var enforceDuplicates = true
    var upsertCalls = 0
        private set

    override fun observeRules(): Flow<List<MuteRule>> {
        val error = rulesError
        return if (error != null) {
            kotlinx.coroutines.flow.flow { throw error }
        } else {
            rulesFlow
        }
    }

    override suspend fun upsertRule(rule: MuteRule) {
        upsertCalls++
        writeError?.let { throw it }
        if (enforceDuplicates && rulesFlow.value.any { it.isEquivalentTo(rule) }) {
            throw com.eagskunst.emmanuel.gamingnews.core.domain.repository.DuplicateMuteRuleException()
        }
        rulesFlow.update { rules -> rules.filterNot { it.id == rule.id } + rule }
    }

    override suspend fun deleteRule(ruleId: String) {
        writeError?.let { throw it }
        rulesFlow.update { rules -> rules.filterNot { it.id == ruleId } }
    }

    private fun MuteRule.isEquivalentTo(other: MuteRule): Boolean {
        if (id == other.id) return false
        if (matchMode != other.matchMode || caseSensitive != other.caseSensitive) return false
        if (scope != other.scope) return false
        return comparableText() == other.comparableText()
    }

    private fun MuteRule.comparableText(): String =
        if (caseSensitive) text else text.lowercase()
}
