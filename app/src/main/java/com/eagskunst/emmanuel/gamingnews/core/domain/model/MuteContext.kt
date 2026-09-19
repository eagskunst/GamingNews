package com.eagskunst.emmanuel.gamingnews.core.domain.model

/**
 * The feed context an article is being filtered for.
 */
sealed interface MuteContext {

    /** A specific news tab. The All news tab is distinct from [MuteScope.Everywhere]. */
    data class NewsTab(val category: NewsCategory) : MuteContext

    /** The Reviews feed. Only global (Everywhere) rules apply, when opted in. */
    data object Reviews : MuteContext

    /** Context-free consumers only apply global (Everywhere) rules. */
    data object Global : MuteContext
}
