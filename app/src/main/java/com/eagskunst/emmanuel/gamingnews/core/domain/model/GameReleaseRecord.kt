package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.util.Date

/**
 * A single IGDB release-date record: one platform-specific release of a game.
 *
 * Records are stored individually (keyed by [releaseId]) so platform-specific dates and
 * cross-page duplicates are preserved; grouping into cards happens only after filtering.
 */
data class GameReleaseRecord(
    val releaseId: Long,
    val gameId: Long,
    val platformId: Int,
    val releaseDate: Date,
    val name: String,
    val coverUrl: String?,
    val gameUrl: String?
)
