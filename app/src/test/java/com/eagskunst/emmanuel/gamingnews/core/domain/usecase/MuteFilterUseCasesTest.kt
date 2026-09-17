package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FailedReviewSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredReviewFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteContext
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteMatchMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewFeedSnapshot
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeMuteRulesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeNewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReviewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MuteFilterUseCasesTest {

    private val newsRepository = FakeNewsRepository()
    private val reviewsRepository = FakeReviewsRepository()
    private val muteRulesRepository = FakeMuteRulesRepository()
    private val userPreferencesRepository = FakeUserPreferencesRepository()

    private fun TestScope.getNewsUseCase() = GetNewsUseCase(
        repository = newsRepository,
        muteRulesRepository = muteRulesRepository,
        dispatchers = TestDispatcherProvider(UnconfinedTestDispatcher(testScheduler))
    )

    private fun TestScope.getReviewsUseCase() = GetReviewsUseCase(
        repository = reviewsRepository,
        muteRulesRepository = muteRulesRepository,
        userPreferencesRepository = userPreferencesRepository,
        dispatchers = TestDispatcherProvider(UnconfinedTestDispatcher(testScheduler))
    )

    // region News filtering

    @Test
    fun `given a matching rule when news is collected then the article is muted and counted`() = runTest {
        val muted = Fixtures.newsArticle(link = "https://a/1", title = "GTA VI trailer")
        val kept = Fixtures.newsArticle(link = "https://a/2", title = "Hollow Knight")
        newsRepository.newsResultFlow.value = Result.Success(listOf(muted, kept))
        muteRulesRepository.rulesFlow.value = listOf(Fixtures.muteRule(text = "gta"))

        getNewsUseCase()(listOf("url")).test {
            val feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(listOf(kept), feed.articles)
            assertEquals(1, feed.mutedCount)
            assertEquals(FilteredFeed.EmptyState.NONE, feed.emptyState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a selected-tabs rule then it only mutes in the selected tab`() = runTest {
        val article = Fixtures.newsArticle(link = "https://a/1", title = "GTA VI trailer")
        newsRepository.newsResultFlow.value = Result.Success(listOf(article))
        muteRulesRepository.rulesFlow.value = listOf(
            Fixtures.muteRule(
                text = "gta",
                scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC))
            )
        )

        getNewsUseCase()(listOf("url"), context = MuteContext.NewsTab(NewsCategory.SONY)).test {
            val feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(listOf(article), feed.articles)
            assertEquals(0, feed.mutedCount)
            cancelAndIgnoreRemainingEvents()
        }

        getNewsUseCase()(listOf("url"), context = MuteContext.NewsTab(NewsCategory.PC)).test {
            val feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(emptyList<Any>(), feed.articles)
            assertEquals(1, feed.mutedCount)
            assertEquals(FilteredFeed.EmptyState.ALL_MUTED, feed.emptyState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a live collection when rules change then the feed re-filters without a new fetch`() = runTest {
        val muted = Fixtures.newsArticle(link = "https://a/1", title = "GTA VI trailer")
        val kept = Fixtures.newsArticle(link = "https://a/2", title = "Hollow Knight")
        newsRepository.newsResultFlow.value = Result.Success(listOf(muted, kept))

        getNewsUseCase()(listOf("url"), context = MuteContext.NewsTab(NewsCategory.ALL)).test {
            var feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(2, feed.articles.size)
            assertEquals(0, feed.mutedCount)

            muteRulesRepository.rulesFlow.value = listOf(Fixtures.muteRule(text = "gta"))

            feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(listOf(kept), feed.articles)
            assertEquals(1, feed.mutedCount)
            assertEquals(1, newsRepository.newsStreamCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a live collection when the search query changes then filtering reapplies without a fetch`() = runTest {
        val muted = Fixtures.newsArticle(link = "https://a/1", title = "GTA VI trailer")
        val otherGta = Fixtures.newsArticle(link = "https://a/2", title = "GTA V discount")
        val zelda = Fixtures.newsArticle(link = "https://a/3", title = "Zelda review")
        newsRepository.newsResultFlow.value = Result.Success(listOf(muted, otherGta, zelda))
        muteRulesRepository.rulesFlow.value = listOf(Fixtures.muteRule(text = "gta"))
        val query = MutableStateFlow("")

        getNewsUseCase()(listOf("url"), searchQuery = query).test {
            var feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(listOf(zelda), feed.articles)
            assertEquals(2, feed.mutedCount)

            // Searching for "zelda" leaves no muted candidates.
            query.value = "zelda"
            feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(listOf(zelda), feed.articles)
            assertEquals(0, feed.mutedCount)

            // Searching for "gta" makes every candidate muted.
            query.value = "gta"
            feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(emptyList<Any>(), feed.articles)
            assertEquals(2, feed.mutedCount)
            assertEquals(FilteredFeed.EmptyState.ALL_MUTED, feed.emptyState)

            assertEquals(1, newsRepository.newsStreamCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a live collection when reveal is toggled then muted articles appear in order without a fetch`() = runTest {
        val muted = Fixtures.newsArticle(link = "https://a/1", title = "GTA VI trailer")
        val kept = Fixtures.newsArticle(link = "https://a/2", title = "Hollow Knight")
        newsRepository.newsResultFlow.value = Result.Success(listOf(muted, kept))
        muteRulesRepository.rulesFlow.value = listOf(Fixtures.muteRule(text = "gta"))
        val reveal = MutableStateFlow(false)

        getNewsUseCase()(listOf("url"), revealMuted = reveal).test {
            var feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(listOf(kept), feed.articles)
            assertFalse(feed.isRevealed)

            reveal.value = true
            feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(listOf(muted, kept), feed.articles)
            assertEquals(setOf("https://a/1"), feed.revealedMutedLinks)
            assertTrue(feed.isRevealed)

            reveal.value = false
            feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(listOf(kept), feed.articles)

            assertEquals(1, newsRepository.newsStreamCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no matching titles then empty state is no search results`() = runTest {
        newsRepository.newsResultFlow.value = Result.Success(
            listOf(Fixtures.newsArticle(title = "Hollow Knight"))
        )
        val query = MutableStateFlow("metroid")

        getNewsUseCase()(listOf("url"), searchQuery = query).test {
            val feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(FilteredFeed.EmptyState.NO_SEARCH_RESULTS, feed.emptyState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given an empty feed then empty state is empty feed`() = runTest {
        newsRepository.newsResultFlow.value = Result.Success(emptyList())

        getNewsUseCase()(listOf("url")).test {
            val feed = (expectMostRecentItem() as Result.Success<FilteredFeed>).data
            assertEquals(FilteredFeed.EmptyState.EMPTY_FEED, feed.emptyState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a failing rules stream then the error is surfaced`() = runTest {
        muteRulesRepository.rulesError = IllegalStateException("db down")
        newsRepository.newsResultFlow.value = Result.Success(
            listOf(Fixtures.newsArticle(title = "GTA VI trailer"))
        )

        getNewsUseCase()(listOf("url")).test {
            val result = expectMostRecentItem()
            assertTrue(result is Result.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Reviews filtering

    @Test
    fun `given opt-in disabled then global rules do not filter reviews`() = runTest {
        val article = Fixtures.newsArticle(link = "https://a/1", title = "GTA VI review")
        reviewsRepository.reviewsResultFlow.value = Result.Success(ReviewFeedSnapshot(listOf(article)))
        muteRulesRepository.rulesFlow.value = listOf(Fixtures.muteRule(text = "gta"))
        userPreferencesRepository.preferencesFlow.value =
            Fixtures.userPreferences(applyGlobalMuteRulesToReviews = false)

        getReviewsUseCase()().test {
            val result = expectMostRecentItem() as Result.Success<*>
            val feed = (result.data as FilteredReviewFeed).feed
            assertEquals(listOf(article), feed.articles)
            assertEquals(0, feed.mutedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given opt-in enabled then global rules filter reviews but selected-tab rules do not`() = runTest {
        val globalMuted = Fixtures.newsArticle(link = "https://a/1", title = "GTA VI review")
        val tabMuted = Fixtures.newsArticle(link = "https://a/2", title = "Starfield review")
        val kept = Fixtures.newsArticle(link = "https://a/3", title = "Hollow Knight review")
        reviewsRepository.reviewsResultFlow.value =
            Result.Success(ReviewFeedSnapshot(listOf(globalMuted, tabMuted, kept)))
        muteRulesRepository.rulesFlow.value = listOf(
            Fixtures.muteRule(id = "global", text = "gta", scope = MuteScope.Everywhere),
            Fixtures.muteRule(
                id = "tab",
                text = "starfield",
                matchMode = MuteMatchMode.CONTAINS,
                scope = MuteScope.SelectedTabs(setOf(NewsCategory.ALL))
            )
        )
        userPreferencesRepository.preferencesFlow.value =
            Fixtures.userPreferences(applyGlobalMuteRulesToReviews = true)

        getReviewsUseCase()().test {
            val result = expectMostRecentItem() as Result.Success<*>
            val filtered = result.data as FilteredReviewFeed
            assertEquals(listOf(tabMuted, kept), filtered.feed.articles)
            assertEquals(1, filtered.feed.mutedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given opt-in enabled when reveal is toggled then muted reviews appear without a fetch`() = runTest {
        val muted = Fixtures.newsArticle(link = "https://a/1", title = "GTA VI review")
        reviewsRepository.reviewsResultFlow.value = Result.Success(ReviewFeedSnapshot(listOf(muted)))
        muteRulesRepository.rulesFlow.value = listOf(Fixtures.muteRule(text = "gta"))
        userPreferencesRepository.preferencesFlow.value =
            Fixtures.userPreferences(applyGlobalMuteRulesToReviews = true)
        val reveal = MutableStateFlow(false)

        getReviewsUseCase()(revealMuted = reveal).test {
            var result = expectMostRecentItem() as Result.Success<*>
            assertEquals(
                FilteredFeed.EmptyState.ALL_MUTED,
                (result.data as FilteredReviewFeed).feed.emptyState
            )

            reveal.value = true
            result = expectMostRecentItem() as Result.Success<*>
            val feed = (result.data as FilteredReviewFeed).feed
            assertEquals(listOf(muted), feed.articles)
            assertEquals(setOf("https://a/1"), feed.revealedMutedLinks)

            assertEquals(1, reviewsRepository.reviewsStreamCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given failed sources when reviews are filtered then the metadata is preserved`() = runTest {
        val failed = FailedReviewSource(
            id = "reviews-ign-en",
            name = "IGN",
            reason = FailedReviewSource.FailureReason.UnsupportedEligibilityMetadata
        )
        val article = Fixtures.newsArticle(title = "GTA VI review")
        reviewsRepository.reviewsResultFlow.value = Result.Success(
            ReviewFeedSnapshot(articles = listOf(article), failedSources = listOf(failed))
        )
        muteRulesRepository.rulesFlow.value = listOf(Fixtures.muteRule(text = "gta"))
        userPreferencesRepository.preferencesFlow.value =
            Fixtures.userPreferences(applyGlobalMuteRulesToReviews = true)

        getReviewsUseCase()().test {
            val result = expectMostRecentItem() as Result.Success<*>
            val filtered = result.data as FilteredReviewFeed
            assertEquals(listOf(failed), filtered.failedSources)
            assertEquals(1, filtered.feed.mutedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion
}
