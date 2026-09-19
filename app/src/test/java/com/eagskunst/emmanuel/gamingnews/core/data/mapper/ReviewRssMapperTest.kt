package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.prof18.rssparser.model.RssItem
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private fun yearOf(date: java.util.Date): Int =
    Calendar.getInstance().apply { time = date }.get(Calendar.YEAR)

private fun rssItem(
    title: String? = "Title",
    link: String? = "https://example.com/article",
    description: String? = "Description",
    image: String? = null,
    pubDate: String? = "Mon, 01 Jan 2024 10:00:00 GMT",
    author: String? = null
) = RssItem(
    guid = null,
    title = title,
    author = author,
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
class ReviewRssMapperTest {

    @Test
    fun `given valid item when toReviewArticle then maps all fields`() {
        val item = rssItem(
            title = "Game Review",
            link = "https://example.com/review",
            description = "<p>This is a great game.</p>",
            image = "https://example.com/image.png",
            pubDate = "Mon, 01 Jan 2024 10:00:00 GMT",
            author = "John Doe"
        )

        val article = item.toReviewArticle("IGN")

        assertNotNull(article)
        assertEquals("Game Review", article?.title)
        assertEquals("https://example.com/review", article?.link)
        assertEquals("This is a great game.", article?.description)
        assertEquals("https://example.com/image.png", article?.imageUrl)
        assertEquals("IGN", article?.sourceName)
        assertEquals("John Doe", article?.author)
        assertEquals(2024, yearOf(article?.publicationDate!!))
    }

    @Test
    fun `given blank author when toReviewArticle then author is null`() {
        val item = rssItem(author = "   ")

        val article = item.toReviewArticle("IGN")

        assertNull(article?.author)
    }

    @Test
    fun `given html tags and entities in description when toReviewArticle then tags are stripped and entities decoded`() {
        val item = rssItem(description = "<p>Great &amp; fun game.</p>")

        val article = item.toReviewArticle("IGN")

        assertEquals("Great & fun game.", article?.description)
    }

    @Test
    fun `given long description when toReviewArticle then truncated with ellipsis`() {
        val longText = "A".repeat(300)
        val item = rssItem(description = longText)

        val article = item.toReviewArticle("IGN")

        assertTrue(article?.description?.endsWith("...") == true)
        assertTrue((article?.description?.length ?: 0) <= 280)
    }

    @Test
    fun `given ftp link when toReviewArticle then returns null`() {
        val item = rssItem(link = "ftp://example.com/review")

        val article = item.toReviewArticle("IGN")

        assertNull(article)
    }

    @Test
    fun `given blank title when toReviewArticle then returns null`() {
        val item = rssItem(title = "   ")

        val article = item.toReviewArticle("IGN")

        assertNull(article)
    }

    @Test
    fun `given blank pubDate when toReviewArticle then uses unknown date sentinel`() {
        val item = rssItem(pubDate = "")

        val article = item.toReviewArticle("IGN")

        assertEquals(UNKNOWN_REVIEW_DATE, article?.publicationDate)
    }

    @Test
    fun `given unparseable pubDate when toReviewArticle then uses unknown date sentinel`() {
        val item = rssItem(pubDate = "not-a-date")

        val article = item.toReviewArticle("IGN")

        assertEquals(UNKNOWN_REVIEW_DATE, article?.publicationDate)
    }

    @Test
    fun `given ISO 8601 date when toReviewArticle then date is parsed`() {
        val item = rssItem(pubDate = "2024-03-15T08:30:00+0000")

        val article = item.toReviewArticle("IGN")

        assertEquals(2024, yearOf(article?.publicationDate!!))
    }
}
