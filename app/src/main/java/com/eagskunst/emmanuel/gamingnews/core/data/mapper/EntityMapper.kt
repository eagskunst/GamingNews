package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.FollowedGameEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameReleaseRecord
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.toMask
import com.eagskunst.emmanuel.gamingnews.core.domain.model.toOffsets

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
    gameUrl = gameUrl,
    hasExactDate = hasExactDate,
    human = human
)

fun GameReleaseRecord.toReleaseEntity(): ReleaseEntity = ReleaseEntity(
    id = releaseId,
    gameId = gameId,
    platformId = platformId,
    name = name,
    coverUrl = coverUrl,
    releaseDate = releaseDate,
    gameUrl = gameUrl,
    hasExactDate = hasExactDate,
    human = human
)

fun FollowedGameEntity.toFollowedGame(): FollowedGame = FollowedGame(
    gameId = gameId,
    name = name,
    coverUrl = coverUrl,
    gameUrl = gameUrl,
    releaseEpochDay = releaseEpochDay,
    offsets = offsets.toOffsets(),
    notifiedOffsets = notifiedOffsets.toOffsets(),
    platformsLabel = platformsLabel,
    followedAt = followedAt,
    lastSyncedAt = lastSyncedAt
)

fun FollowedGame.toFollowedGameEntity(): FollowedGameEntity = FollowedGameEntity(
    gameId = gameId,
    name = name,
    coverUrl = coverUrl,
    gameUrl = gameUrl,
    releaseEpochDay = releaseEpochDay,
    offsets = offsets.toMask(),
    notifiedOffsets = notifiedOffsets.toMask(),
    platformsLabel = platformsLabel,
    followedAt = followedAt,
    lastSyncedAt = lastSyncedAt
)
