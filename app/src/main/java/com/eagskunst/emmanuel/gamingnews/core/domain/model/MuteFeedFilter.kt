package com.eagskunst.emmanuel.gamingnews.core.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * The shared pipeline that applies the search query and the mute rules to one unfiltered
 * article snapshot, so rendered results and muted counts always use the same candidate set.
 */
object MuteFeedFilter {

    fun filter(
        articles: List<NewsArticle>,
        rules: List<MuteRule>,
        context: MuteContext,
        searchQuery: String,
        revealMuted: Boolean
    ): FilteredFeed {
        val query = searchQuery.trim()
        val candidates = if (query.isEmpty()) {
            articles
        } else {
            articles.filter { it.title.contains(query, ignoreCase = true) }
        }

        val matcher = PreparedTitleMuteMatcher(rules, context)
        val mutedLinks = candidates
            .filter { matcher.matches(it.title) }
            .mapTo(HashSet()) { it.link }

        val visible = if (revealMuted) {
            candidates
        } else {
            candidates.filterNot { it.link in mutedLinks }
        }

        val emptyState = when {
            articles.isEmpty() -> FilteredFeed.EmptyState.EMPTY_FEED
            candidates.isEmpty() -> FilteredFeed.EmptyState.NO_SEARCH_RESULTS
            !revealMuted && visible.isEmpty() -> FilteredFeed.EmptyState.ALL_MUTED
            else -> FilteredFeed.EmptyState.NONE
        }

        return FilteredFeed(
            articles = visible,
            mutedCount = mutedLinks.size,
            revealedMutedLinks = if (revealMuted) mutedLinks else emptySet(),
            isRevealed = revealMuted,
            emptyState = emptyState,
            sourceLinks = articles.mapTo(HashSet()) { it.link }
        )
    }
}

/**
 * Rules stream wrapped so a load failure surfaces as data instead of silently becoming an
 * empty mute list.
 */
internal sealed interface RulesLoad {
    data class Loaded(val rules: List<MuteRule>) : RulesLoad
    data class Failed(val error: Throwable) : RulesLoad
}

internal fun Flow<List<MuteRule>>.asRulesLoad(): Flow<RulesLoad> =
    map<List<MuteRule>, RulesLoad> { RulesLoad.Loaded(it) }
        .catch { emit(RulesLoad.Failed(it)) }
