package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.text.Normalizer

/**
 * Shared title matching for mute rules.
 *
 * Matching is Unicode-aware and locale-independent: case-insensitive comparisons rely on
 * per-character case folding ([String.contains]/[String.indexOf] with `ignoreCase`), so inputs
 * are never lowercased ahead of time and case-sensitive rules always compare raw text.
 */
object TitleMuteMatcher {

    private val WHITESPACE_RUN = Regex("\\s+")

    private val WORD_CHAR_TYPES = setOf(
        Character.UPPERCASE_LETTER,
        Character.LOWERCASE_LETTER,
        Character.TITLECASE_LETTER,
        Character.MODIFIER_LETTER,
        Character.OTHER_LETTER,
        Character.DECIMAL_DIGIT_NUMBER,
        Character.LETTER_NUMBER,
        Character.OTHER_NUMBER,
        Character.NON_SPACING_MARK,
        Character.ENCLOSING_MARK,
        Character.COMBINING_SPACING_MARK,
        Character.CONNECTOR_PUNCTUATION
    ).mapTo(HashSet()) { it.toInt() }

    /**
     * Trims, collapses every whitespace run into a single space, and applies Unicode NFC
     * so canonically-equivalent text (e.g. precomposed vs combining accents) still matches.
     * NFC preserves casing, punctuation and accents as displayed.
     */
    fun normalizeWhitespace(text: String): String =
        Normalizer.normalize(text.trim().replace(WHITESPACE_RUN, " "), Normalizer.Form.NFC)

    /**
     * Whether [rule] matches [normalizedTitle]. The title must already be passed through
     * [normalizeWhitespace]; the rule text is normalized inside.
     */
    fun matches(rule: MuteRule, normalizedTitle: String): Boolean {
        val needle = normalizeWhitespace(rule.text)
        return matches(normalizedTitle, needle, rule.matchMode, rule.caseSensitive)
    }

    internal fun matches(
        normalizedTitle: String,
        needle: String,
        mode: MuteMatchMode,
        caseSensitive: Boolean
    ): Boolean {
        if (needle.isEmpty()) return false
        val ignoreCase = !caseSensitive
        return when (mode) {
            MuteMatchMode.CONTAINS -> normalizedTitle.contains(needle, ignoreCase)
            MuteMatchMode.WHOLE_WORD,
            MuteMatchMode.EXACT_PHRASE -> indexOfBounded(normalizedTitle, needle, ignoreCase)
        }
    }

    /**
     * Whether [needle] occurs in [haystack] with both ends on a word boundary: the characters
     * immediately before and after the match must not be Unicode word characters (letters,
     * digits, marks or connector punctuation).
     */
    internal fun indexOfBounded(haystack: String, needle: String, ignoreCase: Boolean): Boolean {
        var index = haystack.indexOf(needle, 0, ignoreCase)
        while (index >= 0) {
            val end = index + needle.length
            val boundedStart = index == 0 || !isWordChar(haystack[index - 1])
            val boundedEnd = end >= haystack.length || !isWordChar(haystack[end])
            if (boundedStart && boundedEnd) return true
            index = haystack.indexOf(needle, index + 1, ignoreCase)
        }
        return false
    }

    internal fun isWordChar(char: Char): Boolean = Character.getType(char) in WORD_CHAR_TYPES
}

/**
 * A mute-rule set prepared for a specific [MuteContext]: applicable rules are resolved once and
 * each rule's text is normalized up front, so a snapshot of titles can be matched without
 * re-normalizing the rules.
 */
class PreparedTitleMuteMatcher(rules: List<MuteRule>, context: MuteContext) {

    private val preparedRules = rules
        .filter { it.scope.appliesTo(context) }
        .map { PreparedRule(it) }

    val isEmpty: Boolean get() = preparedRules.isEmpty()

    /** Normalizes [title] once, then evaluates every applicable rule with OR semantics. */
    fun matches(title: String): Boolean {
        val normalizedTitle = TitleMuteMatcher.normalizeWhitespace(title)
        return preparedRules.any {
            TitleMuteMatcher.matches(normalizedTitle, it.needle, it.mode, it.caseSensitive)
        }
    }

    private class PreparedRule(rule: MuteRule) {
        val needle = TitleMuteMatcher.normalizeWhitespace(rule.text)
        val mode = rule.matchMode
        val caseSensitive = rule.caseSensitive
    }
}
