package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.util.Date

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * Everything the game detail page shows for one IGDB game. Follows still go through
 * [GameRelease] (see [pendingRelease]) so the detail page shares the Releases/watchlist model.
 */
data class GameDetails(
    val gameId: Long,
    val name: String,
    val summary: String?,
    val coverUrl: String?,
    val gameUrl: String?,
    val userRating: GameRating?,
    val criticRating: GameRating?,
    val hypes: Int?,
    val genres: List<String>,
    val modes: List<String>,
    val developers: List<String>,
    val publishers: List<String>,
    val franchise: String?,
    val ageRatings: List<String>,
    /** One entry per platform, dated ones first in chronological order. */
    val platformReleases: List<PlatformRelease>,
    val screenshots: List<String>,
    val videos: List<GameVideo>,
    val similarGames: List<SimilarGame>,
    val expansions: List<String>,
    val dlcs: List<String>,
    val links: List<GameLink>
)

/** A 0–100 score and how many ratings it is built from. */
data class GameRating(val score: Int, val count: Int)

/**
 * A platform the game is listed for. [releaseDate] is null when IGDB has no release row for
 * the platform; approximate dates keep a [releaseDate] for ordering and show [human].
 */
data class PlatformRelease(
    val platformId: Int,
    val platformName: String,
    val releaseDate: Date?,
    val hasExactDate: Boolean,
    val human: String?
) {
    /** UTC calendar day of the release, matching how follows store their day. */
    val epochDay: Long? get() = releaseDate?.let { Math.floorDiv(it.time, MILLIS_PER_DAY) }
}

data class GameVideo(val name: String, val youtubeId: String) {
    val url: String get() = "https://www.youtube.com/watch?v=$youtubeId"
}

data class SimilarGame(val id: Long, val name: String, val coverUrl: String?)

data class GameLink(val type: GameLinkType, val url: String)

enum class GameLinkType { OFFICIAL, STEAM, GOG, EPIC, PLAYSTATION, XBOX, NINTENDO }

/** The next release that has not happened yet (exact or approximate), if any. */
fun GameDetails.nextRelease(todayEpochDay: Long): PlatformRelease? =
    platformReleases
        .filter { release -> release.epochDay?.let { it >= todayEpochDay } == true }
        .minByOrNull { it.epochDay!! }

/**
 * The earliest still-pending day-precision release on a platform the app tracks, shaped as the
 * [GameRelease] the follow flow takes. Null once nothing is left to follow, which is what hides
 * the Follow button for released games; a later port of an old game brings it back.
 */
fun GameDetails.pendingRelease(todayEpochDay: Long, supportedPlatformIds: Set<Int>): GameRelease? {
    val pending = platformReleases.filter { release ->
        release.hasExactDate &&
            release.platformId in supportedPlatformIds &&
            release.epochDay?.let { it >= todayEpochDay } == true
    }
    val day = pending.minOfOrNull { it.epochDay!! } ?: return null
    return GameRelease(
        id = gameId,
        name = name,
        coverUrl = coverUrl,
        releaseDate = Date(day * MILLIS_PER_DAY),
        platforms = pending.filter { it.epochDay == day }.map { it.platformName }.distinct(),
        gameUrl = gameUrl,
        hasExactDate = true,
        human = null
    )
}
