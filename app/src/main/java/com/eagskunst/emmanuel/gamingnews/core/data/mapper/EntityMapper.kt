package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle

fun ArticleEntity.toNewsArticle(): NewsArticle = NewsArticle(
    link = link,
    title = title,
    description = description,
    imageUrl = imageUrl,
    publicationDate = publicationDate,
    sourceName = sourceName
)

fun NewsArticle.toArticleEntity(): ArticleEntity = ArticleEntity(
    link = link,
    title = title,
    description = description,
    imageUrl = imageUrl,
    publicationDate = publicationDate,
    sourceName = sourceName
)

fun ReleaseEntity.toGameRelease(): GameRelease = GameRelease(
    id = id,
    name = name,
    coverUrl = coverUrl,
    releaseDate = releaseDate,
    platforms = platforms.split(", ").filter { it.isNotBlank() },
    gameUrl = gameUrl
)

fun GameRelease.toReleaseEntity(): ReleaseEntity = ReleaseEntity(
    id = id,
    name = name,
    coverUrl = coverUrl,
    releaseDate = releaseDate,
    platforms = platforms.joinToString(", "),
    gameUrl = gameUrl
)
