package com.eagskunst.emmanuel.gamingnews.core.domain.model

import java.util.Date

/**
 * A grouped release card. [hasExactDate] is true only when every record in the group has
 * day-precision; approximate cards show [human] instead of a formatted day and can't be
 * followed for reminders.
 */
data class GameRelease(
    val id: Long,
    val name: String,
    val coverUrl: String?,
    val releaseDate: Date,
    val platforms: List<String>,
    val gameUrl: String?,
    val hasExactDate: Boolean,
    val human: String?
)
