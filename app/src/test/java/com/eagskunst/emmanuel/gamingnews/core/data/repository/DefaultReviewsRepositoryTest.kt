package com.eagskunst.emmanuel.gamingnews.core.data.repository

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ReviewCatalogDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.RssRemoteDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewEligibilityPolicy
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewFeedSnapshot
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewSource
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.prof18.rssparser.model.RssChannel
import com.prof18.rssparser.model.RssItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultReviewsRepositoryTest {

    private val rssRemoteDataSource: RssRemoteDataSource = mockk()
    private val catalogDataSource: ReviewCatalogDataSource = mockk()
    private val repository = DefaultReviewsRepository(
        rssRemoteDataSource = rssRemoteDataSource,
        catalogDataSource = catalogDataSource,
        dispatchers = TestDispatcherProvider()
    )

    @Test
    fun `given gaming source when reviewsStream then returns mapped articles sorted newest first`() = runTest {
        val source = reviewSource("es", ReviewEligibilityPolicy.GamingFeedReviews)
        every { catalogDataSource.selectedSource(any()) } returns source
        coEvery { rssRemoteDataSource.fetchChannel(source.url) } returns channel(
            item("Old Game", "https://example.com/old", "Mon, 01 Jan 2024 10:00:00 GMT", "Author One"),
            item("New Game", "https://example.com/new", "Wed, 03 Jan 2024 10:00:00 GMT", "Author Two")
        )

        repository.reviewsStream().test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Success
            assertEquals(listOf("New Game", "Old Game"), result.data.articles.map { it.title })
            assertEquals("Author Two", result.data.articles[0].author)
            assertTrue(result.data.failedSources.isEmpty())

            awaitComplete()
        }
    }

    @Test
    fun `given unsupported mixed content source when reviewsStream then returns error`() = runTest {
        val source = reviewSource("en", ReviewEligibilityPolicy.UnsupportedMixedContent)
        every { catalogDataSource.selectedSource(any()) } returns source

        repository.reviewsStream().test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Error
            assertTrue(result.exception is UnsupportedOperationException)

            awaitComplete()
        }
    }

    @Test
    fun `given network error and no cache when reviewsStream then returns error`() = runTest {
        val source = reviewSource("es", ReviewEligibilityPolicy.GamingFeedReviews)
        every { catalogDataSource.selectedSource(any()) } returns source
        coEvery { rssRemoteDataSource.fetchChannel(source.url) } throws RuntimeException("network failure")

        repository.reviewsStream().test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Error
            assertEquals("network failure", result.exception.message)

            awaitComplete()
        }
    }

    @Test
    fun `given cached fresh data when reviewsStream not forceRefresh then returns cached success without loading`() = runTest {
        val source = reviewSource("es", ReviewEligibilityPolicy.GamingFeedReviews)
        every { catalogDataSource.selectedSource(any()) } returns source
        coEvery { rssRemoteDataSource.fetchChannel(source.url) } returns channel(
            item("Cached", "https://example.com/cached", "Mon, 01 Jan 2024 10:00:00 GMT")
        )

        repository.reviewsStream().test {
            assertEquals(Result.Loading, awaitItem())
            assertTrue(awaitItem() is Result.Success)
            awaitComplete()
        }

        repository.reviewsStream(forceRefresh = false).test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals("Cached", (result as Result.Success).data.articles.first().title)
            awaitComplete()
        }
    }

    @Test
    fun `given cache exists when forceRefresh fails then returns cached data with failed source`() = runTest {
        val source = reviewSource("es", ReviewEligibilityPolicy.GamingFeedReviews)
        every { catalogDataSource.selectedSource(any()) } returns source
        var callCount = 0
        coEvery { rssRemoteDataSource.fetchChannel(source.url) } answers {
            callCount++
            if (callCount == 1) {
                channel(item("Cached", "https://example.com/cached", "Mon, 01 Jan 2024 10:00:00 GMT"))
            } else {
                throw RuntimeException("refresh failed")
            }
        }

        repository.reviewsStream().test {
            assertEquals(Result.Loading, awaitItem())
            assertTrue(awaitItem() is Result.Success)
            awaitComplete()
        }

        repository.reviewsStream(forceRefresh = true).test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Success
            assertEquals("Cached", result.data.articles.first().title)
            assertEquals(1, result.data.failedSources.size)

            awaitComplete()
        }

        repository.reviewsStream(forceRefresh = false).test {
            val result = awaitItem() as Result.Success
            assertEquals("Cached", result.data.articles.first().title)
            assertTrue(result.data.failedSources.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `given duplicate urls when reviewsStream then deduplicates preserving first`() = runTest {
        val source = reviewSource("es", ReviewEligibilityPolicy.GamingFeedReviews)
        every { catalogDataSource.selectedSource(any()) } returns source
        coEvery { rssRemoteDataSource.fetchChannel(source.url) } returns channel(
            item("First", "https://example.com/article#fragment", "Mon, 01 Jan 2024 10:00:00 GMT"),
            item("Second", "https://example.com/article", "Tue, 02 Jan 2024 10:00:00 GMT")
        )

        repository.reviewsStream().test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Success
            assertEquals(1, result.data.articles.size)
            assertEquals("Second", result.data.articles.first().title)

            awaitComplete()
        }
    }

    @Test
    fun `given invalid url item when reviewsStream then filters it out`() = runTest {
        val source = reviewSource("es", ReviewEligibilityPolicy.GamingFeedReviews)
        every { catalogDataSource.selectedSource(any()) } returns source
        coEvery { rssRemoteDataSource.fetchChannel(source.url) } returns channel(
            item("Bad", "not-a-url", "Mon, 01 Jan 2024 10:00:00 GMT"),
            item("Good", "https://example.com/good", "Mon, 01 Jan 2024 10:00:00 GMT")
        )

        repository.reviewsStream().test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem() as Result.Success
            assertEquals(listOf("Good"), result.data.articles.map { it.title })

            awaitComplete()
        }
    }

    private fun reviewSource(language: String, policy: ReviewEligibilityPolicy): ReviewSource = ReviewSource(
        id = "reviews-$language",
        language = language,
        name = "Test Source",
        url = "https://example.com/$language/reviews",
        eligibilityPolicy = policy
    )

    private fun channel(vararg items: RssItem): RssChannel = mockk<RssChannel>().apply {
        every { title } returns "Test Channel"
        every { this@apply.items } returns items.toList()
    }

    private fun item(title: String, link: String, pubDate: String, author: String? = null): RssItem =
        mockk<RssItem>(relaxed = true).apply {
            every { this@apply.title } returns title
            every { this@apply.author } returns author
            every { this@apply.link } returns link
            every { this@apply.pubDate } returns pubDate
            every { this@apply.description } returns ""
            every { this@apply.image } returns null
        }
}
