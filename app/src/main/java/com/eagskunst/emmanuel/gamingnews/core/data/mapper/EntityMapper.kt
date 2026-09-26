package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameReleaseRecord
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle

fun ArticleEntity.toNewsArticle(): NewsArticle = NewsArticle(
    link = link,
    title = title,
    description = description,
    imageUrl = imageUrl,
    publicationDate = publicationDate,
    sourceName = sourceName,
    author = author
)

fun NewsArticle.toArticleEntity(): ArticleEntity = ArticleEntity(
    link = link,
    title = title,
    description = description,
    imageUrl = imageUrl,
    publicationDate = publicationDate,
    sourceName = sourceName,
    author = author
)

fun ReleaseEntity.toReleaseRecord(): GameReleaseRecord = GameReleaseRecord(
    releaseId = id,
    gameId = gameId,
    platformId = platformId,
    releaseDate = releaseDate,
    name = name,
    coverUrl = coverUrl,
    gameUrl = gameUrl
)

fun GameReleaseRecord.toReleaseEntity(): ReleaseEntity = ReleaseEntity(
    id = releaseId,
    gameId = gameId,
    platformId = platformId,
    name = name,
    coverUrl = coverUrl,
    releaseDate = releaseDate,
    gameUrl = gameUrl
)
