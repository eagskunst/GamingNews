package com.eagskunst.emmanuel.gamingnews.core.domain.model

/**
 * How a mute rule's text is compared against an article title.
 */
enum class MuteMatchMode {
    /** Literal text anywhere in the title. */
    CONTAINS,

    /** One complete word within the title. Restricted to single-word input. */
    WHOLE_WORD,

    /** Consecutive complete words within a longer title. */
    EXACT_PHRASE
}

/**
 * Where a mute rule applies.
 */
sealed interface MuteScope {

    /** All news tabs, plus Reviews when the user opted in. */
    data object Everywhere : MuteScope

    /** Only the selected news tabs. Never applies to Reviews. */
    data class SelectedTabs(val tabs: Set<NewsCategory>) : MuteScope {
        init {
            require(tabs.isNotEmpty()) { "SelectedTabs requires at least one tab" }
        }
    }

    fun appliesTo(context: MuteContext): Boolean = when (this) {
        Everywhere -> true
        is SelectedTabs -> context is MuteContext.NewsTab && context.category in tabs
    }
}

/**
 * A user-defined rule that mutes articles whose title matches [text].
 * [text] keeps the user's display casing, punctuation and accents.
 */
data class MuteRule(
    val id: String,
    val text: String,
    val matchMode: MuteMatchMode = MuteMatchMode.CONTAINS,
    val caseSensitive: Boolean = false,
    val scope: MuteScope = MuteScope.Everywhere
)
