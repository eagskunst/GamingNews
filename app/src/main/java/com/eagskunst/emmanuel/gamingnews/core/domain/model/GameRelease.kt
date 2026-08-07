package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.util.Date

data class GameRelease(
    val id: Long,
    val name: String,
    val coverUrl: String?,
    val releaseDate: Date,
    val platforms: List<String>,
    val gameUrl: String?
)
