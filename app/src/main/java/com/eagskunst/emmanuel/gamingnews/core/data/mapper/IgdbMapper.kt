package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbCoverDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameReleaseRecord
import java.util.Date

/**
 * Maps one IGDB release-date record to a [GameReleaseRecord], preserving the release-record ID,
 * game ID, platform ID and exact release date. Records are never merged here — grouping is a
 * domain concern applied after filtering.
 */
fun IgdbReleaseDateDto.toReleaseRecord(): GameReleaseRecord? {
    val gameDto = game ?: return null
    val gameName = gameDto.name
    val releaseTimestamp = date
    if (gameName.isNullOrBlank() || releaseTimestamp == null) return null

    return GameReleaseRecord(
        releaseId = id,
        gameId = gameDto.id,
        platformId = platform,
        releaseDate = Date(releaseTimestamp * 1000L),
        name = gameName,
        coverUrl = gameDto.cover?.toCoverBigUrl(),
        gameUrl = gameDto.url
    )
}

private fun IgdbCoverDto.toCoverBigUrl(): String? {
    val rawUrl = url ?: return null
    return when {
        rawUrl.startsWith("http") -> rawUrl.replace("t_thumb", "t_cover_big")
        rawUrl.startsWith("//") -> "https:$rawUrl".replace("t_thumb", "t_cover_big")
        else -> null
    }
}
