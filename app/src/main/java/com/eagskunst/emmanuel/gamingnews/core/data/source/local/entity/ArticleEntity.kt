package com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "articles",
    indices = [Index(value = ["link"], unique = true)]
)
data class ArticleEntity(
    @PrimaryKey val link: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val publicationDate: Date,
    val sourceName: String,
    val savedAt: Date = Date()
)
