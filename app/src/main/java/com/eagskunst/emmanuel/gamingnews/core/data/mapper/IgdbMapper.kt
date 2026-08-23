package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbCoverDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import java.util.Date

fun IgdbReleaseDateDto.toGameRelease(): GameRelease? {
    val gameName = game?.name
    val releaseTimestamp = date
    if (gameName.isNullOrBlank() || releaseTimestamp == null) return null

    return GameRelease(
        id = game.id ?: id,
        name = gameName,
        coverUrl = game.cover?.toCoverBigUrl(),
        releaseDate = Date(releaseTimestamp * 1000L),
        platforms = listOfNotNull(platform.toPlatformName()),
        gameUrl = game.url
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

private fun Int.toPlatformName(): String? = when (this) {
    6 -> "PC"
    48 -> "PS4"
    49 -> "Xbox One"
    130 -> "Nintendo Switch"
    167 -> "PS5"
    169 -> "Xbox Series"
    508 -> "Switch 2"
    else -> null
}
