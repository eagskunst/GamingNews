package com.eagskunst.emmanuel.gamingnews.core.domain.model

/**
 * Validation errors a [MuteRuleDraft] can produce.
 */
enum class MuteRuleValidationError {
    BLANK_TEXT,
    WHOLE_WORD_MULTI_WORD,
    EMPTY_TAB_SELECTION,
    DUPLICATE_RULE
}

/**
 * The editable fields of a [MuteRule]. [id] is null for a new rule and set when editing.
 * Unlike [MuteScope.SelectedTabs], [selectedTabs] may be empty — that state is invalid and is
 * reported as [MuteRuleValidationError.EMPTY_TAB_SELECTION] rather than being unrepresentable.
 */
data class MuteRuleDraft(
    val id: String? = null,
    val text: String = "",
    val matchMode: MuteMatchMode = MuteMatchMode.CONTAINS,
    val caseSensitive: Boolean = false,
    val scopeEverywhere: Boolean = true,
    val selectedTabs: Set<NewsCategory> = emptySet()
) {
    /** The scope this draft describes, or null when it is an invalid empty tab selection. */
    fun scopeOrNull(): MuteScope? = when {
        scopeEverywhere -> MuteScope.Everywhere
        selectedTabs.isNotEmpty() -> MuteScope.SelectedTabs(selectedTabs)
        else -> null
    }
}

/**
 * Input and duplicate validation for mute rules.
 */
object MuteRuleValidator {

    fun validate(draft: MuteRuleDraft, existingRules: List<MuteRule>): Set<MuteRuleValidationError> {
        val errors = mutableSetOf<MuteRuleValidationError>()
        val normalizedText = TitleMuteMatcher.normalizeWhitespace(draft.text)

        if (normalizedText.isEmpty()) {
            errors += MuteRuleValidationError.BLANK_TEXT
        }
        if (draft.matchMode == MuteMatchMode.WHOLE_WORD && normalizedText.any { it.isWhitespace() }) {
            errors += MuteRuleValidationError.WHOLE_WORD_MULTI_WORD
        }
        if (!draft.scopeEverywhere && draft.selectedTabs.isEmpty()) {
            errors += MuteRuleValidationError.EMPTY_TAB_SELECTION
        }
        if (normalizedText.isNotEmpty() && existingRules.any { isDuplicate(draft, it) }) {
            errors += MuteRuleValidationError.DUPLICATE_RULE
        }
        return errors
    }

    /**
     * Two rules are duplicates when they share match mode, case sensitivity and scope, and their
     * normalized texts are equal — compared case-folded for case-insensitive rules and verbatim
     * for case-sensitive ones. A rule never duplicates itself (same [MuteRuleDraft.id]).
     */
    fun isDuplicate(draft: MuteRuleDraft, existing: MuteRule): Boolean {
        if (draft.id != null && draft.id == existing.id) return false
        if (draft.matchMode != existing.matchMode) return false
        if (draft.caseSensitive != existing.caseSensitive) return false
        if (draft.scopeOrNull() != existing.scope) return false
        return comparableText(draft.text, draft.caseSensitive) ==
            comparableText(existing.text, existing.caseSensitive)
    }

    /** Whitespace-normalized comparison form; case-folded only for case-insensitive rules. */
    fun comparableText(text: String, caseSensitive: Boolean): String {
        val normalized = TitleMuteMatcher.normalizeWhitespace(text)
        return if (caseSensitive) normalized else normalized.lowercase()
    }
}
