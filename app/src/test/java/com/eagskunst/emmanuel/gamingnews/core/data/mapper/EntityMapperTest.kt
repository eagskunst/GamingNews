package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
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
            sourceName = "IGN",
            author = "Jane Doe"
        )

        val article = entity.toNewsArticle()

        assertEquals(entity.link, article.link)
        assertEquals(entity.title, article.title)
        assertEquals(entity.description, article.description)
        assertEquals(entity.imageUrl, article.imageUrl)
        assertEquals(entity.publicationDate, article.publicationDate)
        assertEquals(entity.sourceName, article.sourceName)
        assertEquals(entity.author, article.author)
    }

    @Test
    fun `given news article when toArticleEntity then maps every field`() {
        val article = Fixtures.newsArticle(author = "John Smith")

        val entity = article.toArticleEntity()

        assertEquals(article.link, entity.link)
        assertEquals(article.title, entity.title)
        assertEquals(article.description, entity.description)
        assertEquals(article.imageUrl, entity.imageUrl)
        assertEquals(article.publicationDate, entity.publicationDate)
        assertEquals(article.sourceName, entity.sourceName)
        assertEquals(article.author, entity.author)
    }

    @Test
    fun `given article entity without author when round tripped through news article then author stays null`() {
        val entity = ArticleEntity(
            link = "https://example.com/article",
            title = "Some article",
            description = "Description",
            imageUrl = null,
            publicationDate = Date(2_000),
            sourceName = "IGN"
        )

        val roundTripped = entity.toNewsArticle().toArticleEntity()

        assertEquals(entity.author, roundTripped.author)
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
        assertEquals(entity.publicationDate, roundTripped.publicationDate)
        assertEquals(entity.sourceName, roundTripped.sourceName)
    }

    @Test
    fun `given release entity when toReleaseRecord then every field is preserved`() {
        val entity = Fixtures.releaseEntity(
            id = 99L,
            gameId = 42L,
            platformId = 167,
            releaseDate = Date(1_700_000_000_000L)
        )

        val record = entity.toReleaseRecord()

        assertEquals(99L, record.releaseId)
        assertEquals(42L, record.gameId)
        assertEquals(167, record.platformId)
        assertEquals(entity.releaseDate, record.releaseDate)
        assertEquals(entity.name, record.name)
        assertEquals(entity.coverUrl, record.coverUrl)
        assertEquals(entity.gameUrl, record.gameUrl)
    }

    @Test
    fun `given release record when toReleaseEntity then every field is preserved`() {
        val record = Fixtures.gameReleaseRecord(
            releaseId = 7L,
            gameId = 8L,
            platformId = 130,
            releaseDate = Date(4_000L)
        )

        val entity = record.toReleaseEntity()

        assertEquals(record.releaseId, entity.id)
        assertEquals(record.gameId, entity.gameId)
        assertEquals(record.platformId, entity.platformId)
        assertEquals(record.releaseDate, entity.releaseDate)
        assertEquals(record.name, entity.name)
        assertEquals(record.coverUrl, entity.coverUrl)
        assertEquals(record.gameUrl, entity.gameUrl)
    }

    @Test
    fun `given release record when round tripped through release entity then equal`() {
        val record = Fixtures.gameReleaseRecord(
            releaseId = 11L,
            gameId = 12L,
            platformId = 508,
            coverUrl = null,
            releaseDate = Date(9_999L),
            gameUrl = null
        )

        val roundTripped = record.toReleaseEntity().toReleaseRecord()

        assertEquals(record, roundTripped)
    }
}
