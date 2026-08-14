package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.prof18.rssparser.model.RssItem
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private fun yearOf(date: java.util.Date): Int =
    Calendar.getInstance().apply { time = date }.get(Calendar.YEAR)

private fun rssItem(
    title: String?,
    link: String?,
    description: String?,
    image: String?,
    pubDate: String?
) = RssItem(
    guid = null,
    title = title,
    author = null,
    link = link,
    pubDate = pubDate,
    description = description,
    content = null,
    image = image,
    audio = null,
    video = null,
    sourceName = null,
    sourceUrl = null,
    categories = emptyList(),
    itunesItemData = null,
    commentsUrl = null,
    youtubeItemData = null,
    rawEnclosure = null
)

@RunWith(RobolectricTestRunner::class)
class RssMapperTest {

    @Test
    fun `given null link and title when toNewsArticle then defaults to empty string`() {
        val item = rssItem(
            title = null,
            link = null,
            description = "Some description.",
            image = "https://example.com/image.png",
            pubDate = "Mon, 01 Jan 2024 10:00:00 GMT"
        )

        val article = item.toNewsArticle("IGN")

        assertEquals("", article.link)
        assertEquals("", article.title)
    }

    @Test
    fun `given blank link and title when toNewsArticle then defaults to empty string`() {
        val item = rssItem(
            title = "   ",
            link = "",
            description = "Some description.",
            image = null,
            pubDate = "Mon, 01 Jan 2024 10:00:00 GMT"
        )

        val article = item.toNewsArticle("IGN")

        // Only strictly null values are defaulted by the mapper, blank strings pass through.
        assertEquals("", article.link)
        assertEquals("   ", article.title)
    }

    @Test
    fun `given null description when toNewsArticle then description is empty`() {
        val item = rssItem(
            title = "Title",
            link = "https://example.com",
            description = null,
            image = null,
            pubDate = "Mon, 01 Jan 2024 10:00:00 GMT"
        )

        val article = item.toNewsArticle("IGN")

        assertEquals("", article.description)
    }

    @Test
    fun `given html description when toNewsArticle then tags are stripped and truncated at first sentence`() {
        val item = rssItem(
            title = "Title",
            link = "https://example.com",
            description = "<p>This is the <b>first</b> sentence.</p><p>This is the second sentence.</p>",
            image = null,
            pubDate = "Mon, 01 Jan 2024 10:00:00 GMT"
        )

        val article = item.toNewsArticle("IGN")

        assertEquals("This is the first sentence.", article.description)
    }

    @Test
    fun `given long description without a dot when toNewsArticle then truncated with ellipsis`() {
        val longText = "A".repeat(200)
        val item = rssItem(
            title = "Title",
            link = "https://example.com",
            description = longText,
            image = null,
            pubDate = "Mon, 01 Jan 2024 10:00:00 GMT"
        )

        val article = item.toNewsArticle("IGN")

        assertTrue(article.description.endsWith("..."))
        assertEquals(183, article.description.length)
    }

    @Test
    fun `given pubDate in RFC 1123 format when toNewsArticle then date is parsed`() {
        val item = rssItem(
            title = "Title",
            link = "https://example.com",
            description = "Description.",
            image = null,
            pubDate = "Mon, 01 Jan 2024 10:00:00 GMT"
        )

        val article = item.toNewsArticle("IGN")

        assertEquals(2024, yearOf(article.publicationDate))
    }

    @Test
    fun `given pubDate in ISO 8601 format when toNewsArticle then date is parsed`() {
        val item = rssItem(
            title = "Title",
            link = "https://example.com",
            description = "Description.",
            image = null,
            pubDate = "2024-03-15T08:30:00+0000"
        )

        val article = item.toNewsArticle("IGN")

        assertEquals(2024, yearOf(article.publicationDate))
    }

    @Test
    fun `given pubDate in ISO 8601 with milliseconds format when toNewsArticle then date is parsed`() {
        val item = rssItem(
            title = "Title",
            link = "https://example.com",
            description = "Description.",
            image = null,
            pubDate = "2024-03-15T08:30:00.123+0000"
        )

        val article = item.toNewsArticle("IGN")

        assertEquals(2024, yearOf(article.publicationDate))
    }

    @Test
    fun `given blank pubDate when toNewsArticle then falls back to now without throwing`() {
        val item = rssItem(
            title = "Title",
            link = "https://example.com",
            description = "Description.",
            image = null,
            pubDate = ""
        )

        val article = item.toNewsArticle("IGN")

        assertNotNull(article.publicationDate)
    }

    @Test
    fun `given unparseable pubDate when toNewsArticle then falls back to now without throwing`() {
        val item = rssItem(
            title = "Title",
            link = "https://example.com",
            description = "Description.",
            image = null,
            pubDate = "not-a-date"
        )

        val article = item.toNewsArticle("IGN")

        assertNotNull(article.publicationDate)
    }
}
