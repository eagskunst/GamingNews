package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMapperTest {

    @Test
    fun `given article entity when toNewsArticle then maps every field`() {
        val entity = ArticleEntity(
            link = "https://example.com/article",
            title = "Some article",
            description = "Description",
            imageUrl = "https://example.com/image.png",
            publicationDate = Date(1_000),
            sourceName = "IGN"
        )

        val article = entity.toNewsArticle()

        assertEquals(entity.link, article.link)
        assertEquals(entity.title, article.title)
        assertEquals(entity.description, article.description)
        assertEquals(entity.imageUrl, article.imageUrl)
        assertEquals(entity.publicationDate, article.publicationDate)
        assertEquals(entity.sourceName, article.sourceName)
    }

    @Test
    fun `given news article when toArticleEntity then maps every field`() {
        val article = Fixtures.newsArticle()

        val entity = article.toArticleEntity()

        assertEquals(article.link, entity.link)
        assertEquals(article.title, entity.title)
        assertEquals(article.description, entity.description)
        assertEquals(article.imageUrl, entity.imageUrl)
        assertEquals(article.publicationDate, entity.publicationDate)
        assertEquals(article.sourceName, entity.sourceName)
    }

    @Test
    fun `given article entity when round tripped through news article then equal`() {
        val entity = ArticleEntity(
            link = "https://example.com/article",
            title = "Some article",
            description = "Description",
            imageUrl = null,
            publicationDate = Date(2_000),
            sourceName = "IGN"
        )

        val roundTripped = entity.toNewsArticle().toArticleEntity()

        assertEquals(entity.link, roundTripped.link)
        assertEquals(entity.title, roundTripped.title)
        assertEquals(entity.description, roundTripped.description)
        assertEquals(entity.imageUrl, roundTripped.imageUrl)
        assertEquals(entity.publicationDate, roundTripped.publicationDate)
        assertEquals(entity.sourceName, roundTripped.sourceName)
    }

    @Test
    fun `given release entity with single platform when toGameRelease then platforms has one element`() {
        val entity = ReleaseEntity(
            id = 1L,
            name = "Some Game",
            coverUrl = "https://example.com/cover.png",
            releaseDate = Date(0),
            platforms = "PC",
            gameUrl = "https://example.com/game"
        )

        val release = entity.toGameRelease()

        assertEquals(listOf("PC"), release.platforms)
    }

    @Test
    fun `given release entity with multiple platforms when toGameRelease then platforms are split by comma space`() {
        val entity = ReleaseEntity(
            id = 1L,
            name = "Some Game",
            coverUrl = "https://example.com/cover.png",
            releaseDate = Date(0),
            platforms = "PC, PS4, Xbox One",
            gameUrl = "https://example.com/game"
        )

        val release = entity.toGameRelease()

        assertEquals(listOf("PC", "PS4", "Xbox One"), release.platforms)
    }

    @Test
    fun `given release entity with empty platforms when toGameRelease then platforms is empty list`() {
        val entity = ReleaseEntity(
            id = 1L,
            name = "Some Game",
            coverUrl = "https://example.com/cover.png",
            releaseDate = Date(0),
            platforms = "",
            gameUrl = "https://example.com/game"
        )

        val release = entity.toGameRelease()

        assertEquals(emptyList<String>(), release.platforms)
    }

    @Test
    fun `given game release when toReleaseEntity then platforms are joined with comma space`() {
        val release = Fixtures.gameRelease(platforms = listOf("PC", "PS4", "Xbox One"))

        val entity = release.toReleaseEntity()

        assertEquals("PC, PS4, Xbox One", entity.platforms)
        assertEquals(release.id, entity.id)
        assertEquals(release.name, entity.name)
        assertEquals(release.coverUrl, entity.coverUrl)
        assertEquals(release.releaseDate, entity.releaseDate)
        assertEquals(release.gameUrl, entity.gameUrl)
    }

    @Test
    fun `given game release with empty platforms when toReleaseEntity then platforms is empty string`() {
        val release = Fixtures.gameRelease(platforms = emptyList())

        val entity = release.toReleaseEntity()

        assertEquals("", entity.platforms)
    }

    @Test
    fun `given game release when round tripped through release entity then equal`() {
        val release = Fixtures.gameRelease(platforms = listOf("PC", "Nintendo Switch"))

        val roundTripped = release.toReleaseEntity().toGameRelease()

        assertEquals(release, roundTripped)
    }
}
