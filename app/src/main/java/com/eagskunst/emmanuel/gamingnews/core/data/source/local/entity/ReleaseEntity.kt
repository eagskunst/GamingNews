package com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "releases")
data class ReleaseEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val coverUrl: String?,
    val releaseDate: Date,
    val platforms: String,
    val gameUrl: String?,
    val fetchedAt: Date = Date()
)
