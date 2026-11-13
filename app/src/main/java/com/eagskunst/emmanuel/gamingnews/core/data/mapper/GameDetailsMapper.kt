package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IGDB_DATE_FORMAT_YYYYMMDD
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameDetailsDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbPlatformDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameDetails
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameLink
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameLinkType
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRating
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameVideo
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.SimilarGame
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import java.util.Date
import kotlin.math.roundToInt

private const val SIZE_COVER = "t_cover_big"
private const val SIZE_SCREENSHOT = "t_screenshot_big"

/** Maps an IGDB game to [GameDetails]; null when the game has no usable name. */
fun IgdbGameDetailsDto.toGameDetails(catalog: PlatformCatalog): GameDetails? {
    val gameName = name?.takeIf { it.isNotBlank() } ?: return null
    return GameDetails(
        gameId = id,
        name = gameName,
        summary = summary?.takeIf { it.isNotBlank() },
        coverUrl = cover?.url.toIgdbImageUrl(SIZE_COVER),
        gameUrl = url,
        userRating = toRating(rating, ratingCount),
        criticRating = toRating(aggregatedRating, aggregatedRatingCount),
        hypes = hypes?.takeIf { it > 0 },
        genres = genres.mapNotNull { it.name },
        modes = gameModes.mapNotNull { it.name },
        developers = involvedCompanies.filter { it.developer }.mapNotNull { it.company?.name },
        publishers = involvedCompanies.filter { it.publisher }.mapNotNull { it.company?.name },
        franchise = franchises.firstNotNullOfOrNull { it.name },
        ageRatings = ageRatings.mapNotNull { rating ->
            val organization = rating.organization?.name ?: return@mapNotNull null
            val category = rating.ratingCategory?.rating ?: return@mapNotNull null
            "$organization $category"
        },
        platformReleases = toPlatformReleases(catalog),
        screenshots = screenshots.mapNotNull { it.url.toIgdbImageUrl(SIZE_SCREENSHOT) },
        videos = videos.mapNotNull { video ->
            val videoId = video.videoId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            GameVideo(name = video.name?.takeIf { it.isNotBlank() } ?: gameName, youtubeId = videoId)
        },
        similarGames = similarGames.mapNotNull { game ->
            val similarName = game.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            SimilarGame(game.id, similarName, game.cover?.url.toIgdbImageUrl(SIZE_COVER))
        },
        expansions = expansions.mapNotNull { it.name?.trimParentName(gameName) },
        dlcs = dlcs.mapNotNull { it.name?.trimParentName(gameName) },
        links = websites.mapNotNull { website ->
            val type = website.type?.id?.toLinkType() ?: return@mapNotNull null
            val linkUrl = website.url?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            GameLink(type, linkUrl)
        }.sortedBy { it.type.ordinal }
    )
}

private fun toRating(score: Double?, count: Int?): GameRating? =
    if (score == null || count == null || count <= 0) null else GameRating(score.roundToInt(), count)

/**
 * One row per platform: the earliest dated IGDB row wins (regional rows are collapsed to the
 * first launch), and platforms the game lists without any release row are kept with no date.
 */
private fun IgdbGameDetailsDto.toPlatformReleases(catalog: PlatformCatalog): List<PlatformRelease> {
    val earliestByPlatform = releaseDates
        .filter { it.platform != null && it.date != null }
        .groupBy { it.platform!!.id }
        .mapValues { (_, rows) -> rows.minBy { it.date!! } }
    val dated = earliestByPlatform.values
        .sortedBy { it.date }
        .map { it.toPlatformRelease(catalog) }
    val undated = (platforms + releaseDates.mapNotNull { it.platform })
        .distinctBy { it.id }
        .filter { it.id !in earliestByPlatform }
        .map { PlatformRelease(it.id, it.displayName(catalog), null, false, null) }
    return dated + undated
}

private fun IgdbGameReleaseDateDto.toPlatformRelease(catalog: PlatformCatalog) = PlatformRelease(
    platformId = platform!!.id,
    platformName = platform.displayName(catalog),
    releaseDate = Date(date!! * 1000L),
    hasExactDate = dateFormat == IGDB_DATE_FORMAT_YYYYMMDD,
    human = human
)

/** The app's own platform name when it tracks the platform, so labels match the Releases tab. */
private fun IgdbPlatformDto.displayName(catalog: PlatformCatalog): String =
    catalog.find(id)?.displayName ?: abbreviation ?: name ?: catalog.displayName(id)

/** IGDB website type IDs for the links the page shows; everything else (socials, wikis) is dropped. */
private fun Int.toLinkType(): GameLinkType? = when (this) {
    1 -> GameLinkType.OFFICIAL
    13 -> GameLinkType.STEAM
    16 -> GameLinkType.EPIC
    17 -> GameLinkType.GOG
    22 -> GameLinkType.XBOX
    23 -> GameLinkType.PLAYSTATION
    24 -> GameLinkType.NINTENDO
    else -> null
}

/** IGDB image URLs come protocol-relative and thumbnail-sized; swap in the wanted [size]. */
internal fun String?.toIgdbImageUrl(size: String): String? {
    val rawUrl = this ?: return null
    return when {
        rawUrl.startsWith("http") -> rawUrl.replace("t_thumb", size)
        rawUrl.startsWith("//") -> "https:$rawUrl".replace("t_thumb", size)
        else -> null
    }
}

/**
 * IGDB prefixes add-ons with the parent game ("Elden Ring: Shadow of the Erdtree",
 * "The Witcher 3: Wild Hunt - Blood and Wine"). The page already shows the parent, so drop it.
 */
internal fun String.trimParentName(parent: String): String {
    if (!startsWith(parent, ignoreCase = true)) return this
    val rest = substring(parent.length).trimStart(' ', ':', '-', '–', '—')
    return rest.ifBlank { this }
}
