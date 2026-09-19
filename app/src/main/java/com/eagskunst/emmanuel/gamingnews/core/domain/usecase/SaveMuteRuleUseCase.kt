package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleDraft
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleValidationError
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRuleValidator
import com.eagskunst.emmanuel.gamingnews.core.domain.model.TitleMuteMatcher
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.DuplicateMuteRuleException
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.MuteRulesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

sealed interface SaveMuteRuleResult {
    data object Success : SaveMuteRuleResult
    data class Invalid(val errors: Set<MuteRuleValidationError>) : SaveMuteRuleResult
    data class Failure(val error: Throwable) : SaveMuteRuleResult
}

/**
 * Validates a [MuteRuleDraft] against the current rules and saves it atomically.
 * A draft with a null [MuteRuleDraft.id] creates a new rule; a set id updates in place.
 */
class SaveMuteRuleUseCase(
    private val repository: MuteRulesRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(draft: MuteRuleDraft): SaveMuteRuleResult =
        withContext(dispatchers.default) {
            val existing = try {
                repository.observeRules().first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return@withContext SaveMuteRuleResult.Failure(e)
            }

            val errors = MuteRuleValidator.validate(draft, existing)
            if (errors.isNotEmpty()) return@withContext SaveMuteRuleResult.Invalid(errors)

            val scope = draft.scopeOrNull()
                ?: return@withContext SaveMuteRuleResult.Invalid(
                    setOf(MuteRuleValidationError.EMPTY_TAB_SELECTION)
                )
            val rule = MuteRule(
                id = draft.id ?: UUID.randomUUID().toString(),
                text = TitleMuteMatcher.normalizeWhitespace(draft.text),
                matchMode = draft.matchMode,
                caseSensitive = draft.caseSensitive,
                scope = scope
            )
            try {
                repository.upsertRule(rule)
                SaveMuteRuleResult.Success
            } catch (e: DuplicateMuteRuleException) {
                SaveMuteRuleResult.Invalid(setOf(MuteRuleValidationError.DUPLICATE_RULE))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SaveMuteRuleResult.Failure(e)
            }
        }
}
