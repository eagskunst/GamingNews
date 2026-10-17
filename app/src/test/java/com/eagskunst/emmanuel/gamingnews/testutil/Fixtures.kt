package com.eagskunst.emmanuel.gamingnews.testutil

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.FollowedGameEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameReleaseRecord
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteMatchMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
import com.eagskunst.emmanuel.gamingnews.core.domain.model.toMask
import java.util.Date

/**
 * Fixture factories for domain models, to avoid repeating literals across test classes.
 */
object Fixtures {

    fun newsArticle(
        link: String = "https://example.com/article",
        title: String = "Some article",
        description: String = "Description",
        imageUrl: String? = "https://example.com/image.png",
        publicationDate: Date = Date(0),
        sourceName: String = "IGN",
        author: String? = null
    ) = NewsArticle(
        link = link,
        title = title,
        description = description,
        imageUrl = imageUrl,
        publicationDate = publicationDate,
        sourceName = sourceName,
        author = author
    )

    fun gameRelease(
        id: Long = 1L,
        name: String = "Some Game",
        coverUrl: String? = "https://example.com/cover.png",
        releaseDate: Date = Date(0),
        platforms: List<String> = listOf("PC"),
        gameUrl: String? = "https://example.com/game",
        hasExactDate: Boolean = true,
        human: String? = null
    ) = GameRelease(
        id = id,
        name = name,
        coverUrl = coverUrl,
        releaseDate = releaseDate,
        platforms = platforms,
        gameUrl = gameUrl,
        hasExactDate = hasExactDate,
        human = human
    )

    fun gameReleaseRecord(
        releaseId: Long = 1L,
        gameId: Long = 1L,
        platformId: Int = 6,
        releaseDate: Date = Date(0),
        name: String = "Some Game",
        coverUrl: String? = "https://example.com/cover.png",
        gameUrl: String? = "https://example.com/game",
        hasExactDate: Boolean = true,
        human: String? = null
    ) = GameReleaseRecord(
        releaseId = releaseId,
        gameId = gameId,
        platformId = platformId,
        releaseDate = releaseDate,
        name = name,
        coverUrl = coverUrl,
        gameUrl = gameUrl,
        hasExactDate = hasExactDate,
        human = human
    )

    fun releaseEntity(
        id: Long = 1L,
        gameId: Long = 1L,
        platformId: Int = 6,
        name: String = "Some Game",
        coverUrl: String? = "https://example.com/cover.png",
        releaseDate: Date = Date(0),
        gameUrl: String? = "https://example.com/game",
        hasExactDate: Boolean = true,
        human: String? = null
    ) = ReleaseEntity(
        id = id,
        gameId = gameId,
        platformId = platformId,
        name = name,
        coverUrl = coverUrl,
        releaseDate = releaseDate,
        gameUrl = gameUrl,
        hasExactDate = hasExactDate,
        human = human
    )

    fun followedGame(
        gameId: Long = 1L,
        name: String = "Some Game",
        coverUrl: String? = "https://example.com/cover.png",
        gameUrl: String? = "https://example.com/game",
        releaseEpochDay: Long = 20_000L,
        offsets: Set<ReminderOffset> = setOf(ReminderOffset.RELEASE_DAY),
        notifiedOffsets: Set<ReminderOffset> = emptySet(),
        platformsLabel: String = "PC",
        followedAt: Date = Date(0),
        lastSyncedAt: Date? = null
    ) = FollowedGame(
        gameId = gameId,
        name = name,
        coverUrl = coverUrl,
        gameUrl = gameUrl,
        releaseEpochDay = releaseEpochDay,
        offsets = offsets,
        notifiedOffsets = notifiedOffsets,
        platformsLabel = platformsLabel,
        followedAt = followedAt,
        lastSyncedAt = lastSyncedAt
    )

    fun followedGameEntity(
        gameId: Long = 1L,
        name: String = "Some Game",
        coverUrl: String? = "https://example.com/cover.png",
        gameUrl: String? = "https://example.com/game",
        releaseEpochDay: Long = 20_000L,
        offsets: Set<ReminderOffset> = setOf(ReminderOffset.RELEASE_DAY),
        notifiedOffsets: Set<ReminderOffset> = emptySet(),
        platformsLabel: String = "PC",
        followedAt: Date = Date(0),
        lastSyncedAt: Date? = null
    ) = FollowedGameEntity(
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

    fun topic(name: String = "RPG") = Topic(name)

    fun muteRule(
        id: String = "rule-1",
        text: String = "gta",
        matchMode: MuteMatchMode = MuteMatchMode.CONTAINS,
        caseSensitive: Boolean = false,
        scope: MuteScope = MuteScope.Everywhere
    ) = MuteRule(
        id = id,
        text = text,
        matchMode = matchMode,
        caseSensitive = caseSensitive,
        scope = scope
    )

    fun userPreferences(
        themeMode: com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode = com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.SYSTEM,
        dynamicColor: Boolean = true,
        darkTheme: Boolean? = null,
        loadImages: Boolean = true,
        dailyReminder: Boolean = false,
        dailyReminderHour: Int = 9,
        articleOpenMode: ArticleOpenMode = ArticleOpenMode.READER_MODE,
        applyGlobalMuteRulesToReviews: Boolean = false
    ) = UserPreferences(
        themeMode = darkTheme?.let { if (it) com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.DARK else com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.LIGHT } ?: themeMode,
        dynamicColor = dynamicColor,
        loadImages = loadImages,
        dailyReminder = dailyReminder,
        dailyReminderHour = dailyReminderHour,
        articleOpenMode = articleOpenMode,
        applyGlobalMuteRulesToReviews = applyGlobalMuteRulesToReviews
    )

}
