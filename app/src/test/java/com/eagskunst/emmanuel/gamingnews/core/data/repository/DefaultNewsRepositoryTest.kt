package com.eagskunst.emmanuel.gamingnews.core.data.repository

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.mapper.toNewsArticle
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.RssRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeArticleDao
import com.prof18.rssparser.model.RssChannel
import com.prof18.rssparser.model.RssItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultNewsRepositoryTest {

    private val rssRemoteDataSource: RssRemoteDataSource = mockk()
    private val articleDao = FakeArticleDao()
    private val repository = DefaultNewsRepository(
        rssRemoteDataSource = rssRemoteDataSource,
        articleDao = articleDao,
        dispatchers = TestDispatcherProvider()
    )

    @Test
    fun `newsStream emits loading then success with merged and sorted articles`() = runTest {
        val url1 = "https://example.com/rss1"
        val url2 = "https://example.com/rss2"
        val item1 = rssItem(title = "IGN old", link = "https://ign.com/old", pubDate = "Mon, 01 Jan 2024 12:00:00 GMT")
        val item2 = rssItem(title = "IGN new", link = "https://ign.com/new", pubDate = "Wed, 03 Jan 2024 12:00:00 GMT")
        val item3 = rssItem(title = "Kotaku mid", link = "https://kotaku.com/mid", pubDate = "Tue, 02 Jan 2024 12:00:00 GMT")

        coEvery { rssRemoteDataSource.fetchChannel(url1) } returns channel("IGN", item1, item2)
        coEvery { rssRemoteDataSource.fetchChannel(url2) } returns channel("Kotaku", item3)

        repository.newsStream(listOf(url1, url2)).test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Success
            assertEquals(listOf("IGN new", "Kotaku mid", "IGN old"), result.data.map { it.title })
            assertEquals(listOf("https://ign.com/new", "https://kotaku.com/mid", "https://ign.com/old"), result.data.map { it.link })
            assertEquals(listOf("IGN", "Kotaku", "IGN"), result.data.map { it.sourceName })

            awaitComplete()
        }

        coVerify { rssRemoteDataSource.fetchChannel(url1) }
        coVerify { rssRemoteDataSource.fetchChannel(url2) }
    }

    @Test
    fun `newsStream silently drops failed channels and keeps successful ones`() = runTest {
        val badUrl = "https://example.com/bad"
        val goodUrl = "https://example.com/good"
        val goodItem = rssItem(title = "Good article", link = "https://example.com/article", pubDate = "Mon, 01 Jan 2024 12:00:00 GMT")

        coEvery { rssRemoteDataSource.fetchChannel(badUrl) } throws RuntimeException("boom")
        coEvery { rssRemoteDataSource.fetchChannel(goodUrl) } returns channel("Good Source", goodItem)

        repository.newsStream(listOf(badUrl, goodUrl)).test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Success
            assertEquals(listOf("Good article"), result.data.map { it.title })
            assertEquals(listOf("Good Source"), result.data.map { it.sourceName })

            awaitComplete()
        }
    }

    @Test
    fun `newsStream returns cached success immediately on second call without loading when not forceRefresh`() = runTest {
        val url = "https://example.com/rss"
        val item = rssItem(title = "Cached article", link = "https://example.com/cached", pubDate = "Mon, 01 Jan 2024 12:00:00 GMT")

        coEvery { rssRemoteDataSource.fetchChannel(url) } returns channel("Source", item)

        repository.newsStream(listOf(url)).test {
            assertEquals(Result.Loading, awaitItem())
            assertTrue(awaitItem() is Result.Success)
            awaitComplete()
        }

        repository.newsStream(listOf(url), forceRefresh = false).test {
            val first = awaitItem()
            assertTrue(first is Result.Success)
            assertEquals(listOf("Cached article"), (first as Result.Success).data.map { it.title })
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `newsStream with forceRefresh true emits loading and fetches fresh articles even when cache exists`() = runTest {
        val url = "https://example.com/rss"
        val item1 = rssItem(title = "Old article", link = "https://example.com/old", pubDate = "Mon, 01 Jan 2024 12:00:00 GMT")
        val item2 = rssItem(title = "Fresh article", link = "https://example.com/fresh", pubDate = "Tue, 02 Jan 2024 12:00:00 GMT")

        coEvery { rssRemoteDataSource.fetchChannel(url) } returns channel("Source", item1)

        repository.newsStream(listOf(url)).test {
            assertEquals(Result.Loading, awaitItem())
            assertTrue(awaitItem() is Result.Success)
            awaitComplete()
        }

        coEvery { rssRemoteDataSource.fetchChannel(url) } returns channel("Source", item2)

        repository.newsStream(listOf(url), forceRefresh = true).test {
            assertEquals(Result.Loading, awaitItem())
            val success = awaitItem() as Result.Success
            assertEquals(listOf("Fresh article"), success.data.map { it.title })
            awaitComplete()
        }
    }


    @Test
    fun `newsStream emits success with empty list when every channel fetch fails and there is no cache`() = runTest {
        val url = "https://example.com/rss"

        coEvery { rssRemoteDataSource.fetchChannel(url) } throws RuntimeException("network failure")

        repository.newsStream(listOf(url)).test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Success
            assertTrue(result.data.isEmpty())

            awaitComplete()
        }
    }

    @Test
    fun `savedArticlesStream maps dao entities to news articles`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/saved")
        articleDao.insert(article.toArticleEntity())

        repository.savedArticlesStream().test {
            assertEquals(listOf(article), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saveArticle inserts the mapped entity into the dao`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/save", title = "Save me")

        repository.saveArticle(article)

        val entity = articleDao.getByLink(article.link)
        assertEquals(article.link, entity?.link)
        assertEquals(article.title, entity?.title)
        assertEquals(article.description, entity?.description)
    }

    @Test
    fun `removeArticle deletes the mapped entity from the dao`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/delete")
        repository.saveArticle(article)
        assertTrue(articleDao.getByLink(article.link) != null)

        repository.removeArticle(article)

        assertNull(articleDao.getByLink(article.link))
    }

    @Test
    fun `isArticleSaved returns true when the article exists in the dao and false otherwise`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/check")

        assertFalse(repository.isArticleSaved(article))

        repository.saveArticle(article)

        assertTrue(repository.isArticleSaved(article))
    }

    private fun channel(title: String, vararg items: RssItem): RssChannel = mockk<RssChannel>().apply {
        every { this@apply.title } returns title
        every { this@apply.items } returns items.toList()
    }

    private fun rssItem(title: String, link: String, pubDate: String): RssItem = mockk<RssItem>(relaxed = true).apply {
        every { this@apply.title } returns title
        every { this@apply.link } returns link
        every { this@apply.pubDate } returns pubDate
        every { this@apply.image } returns null
        every { this@apply.description } returns ""
    }

}
