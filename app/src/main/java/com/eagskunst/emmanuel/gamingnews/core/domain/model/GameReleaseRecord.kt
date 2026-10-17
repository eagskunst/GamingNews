package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.util.Date

/**
 * A single IGDB release-date record: one platform-specific release of a game.
 *
 * Records are stored individually (keyed by [releaseId]) so platform-specific dates and
 * cross-page duplicates are preserved; grouping into cards happens only after filtering.
 *
 * [hasExactDate] is true only when IGDB reports a full day-precision date (`d != null`);
 * approximate records (TBA / year / quarter) still carry a [releaseDate] for ordering, and
 * their [human] label is what the UI displays.
 */
data class GameReleaseRecord(
    val releaseId: Long,
    val gameId: Long,
    val platformId: Int,
    val releaseDate: Date,
    val name: String,
    val coverUrl: String?,
    val gameUrl: String?,
    val hasExactDate: Boolean,
    val human: String?
)
