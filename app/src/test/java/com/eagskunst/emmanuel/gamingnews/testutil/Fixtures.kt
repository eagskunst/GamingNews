package com.eagskunst.emmanuel.gamingnews.testutil

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.model.UserPreferences
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
        sourceName: String = "IGN"
    ) = NewsArticle(
        link = link,
        title = title,
        description = description,
        imageUrl = imageUrl,
        publicationDate = publicationDate,
        sourceName = sourceName
    )

    fun gameRelease(
        id: Long = 1L,
        name: String = "Some Game",
        coverUrl: String? = "https://example.com/cover.png",
        releaseDate: Date = Date(0),
        platforms: List<String> = listOf("PC"),
        gameUrl: String? = "https://example.com/game"
    ) = GameRelease(
        id = id,
        name = name,
        coverUrl = coverUrl,
        releaseDate = releaseDate,
        platforms = platforms,
        gameUrl = gameUrl
    )

    fun topic(name: String = "RPG") = Topic(name)

    fun userPreferences(
        themeMode: com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode = com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.SYSTEM,
        dynamicColor: Boolean = true,
        darkTheme: Boolean? = null,
        loadImages: Boolean = true,
        dailyReminder: Boolean = false,
        dailyReminderHour: Int = 9,
        articleOpenMode: ArticleOpenMode = ArticleOpenMode.READER_MODE
    ) = UserPreferences(
        themeMode = darkTheme?.let { if (it) com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.DARK else com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.LIGHT } ?: themeMode,
        dynamicColor = dynamicColor,
        loadImages = loadImages,
        dailyReminder = dailyReminder,
        dailyReminderHour = dailyReminderHour,
        articleOpenMode = articleOpenMode
    )

}
