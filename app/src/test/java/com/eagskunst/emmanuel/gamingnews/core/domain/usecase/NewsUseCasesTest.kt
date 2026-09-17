package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeMuteRulesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeNewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsUseCasesTest {

    private val fakeRepository = FakeNewsRepository()
    private val fakeMuteRulesRepository = FakeMuteRulesRepository()

    private fun createGetNewsUseCase(scheduler: TestCoroutineScheduler) = GetNewsUseCase(
        repository = fakeRepository,
        muteRulesRepository = fakeMuteRulesRepository,
        dispatchers = TestDispatcherProvider(UnconfinedTestDispatcher(scheduler))
    )

    @Test
    fun `when getNewsUseCase is invoked then it returns repository newsStream articles unfiltered`() = runTest {
        val expectedArticles = listOf(
            Fixtures.newsArticle(link = "https://example.com/1", title = "Article 1"),
            Fixtures.newsArticle(link = "https://example.com/2", title = "Article 2")
        )
        fakeRepository.newsResultFlow.value = Result.Success(expectedArticles)

        val result = createGetNewsUseCase(testScheduler)(listOf("url1", "url2")).first()

        assertEquals(Result.Success::class.java, result::class.java)
        val feed = (result as Result.Success<FilteredFeed>).data
        assertEquals(expectedArticles, feed.articles)
        assertEquals(0, feed.mutedCount)
    }

    @Test
    fun `when getSavedArticlesUseCase is invoked then it returns saved list`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/saved")
        fakeRepository.saveArticle(article)

        val useCase = GetSavedArticlesUseCase(fakeRepository)

        assertEquals(listOf(article), useCase().first())
    }

    @Test
    fun `when toggleSavedArticleUseCase is invoked on an unsaved article then it saves the article`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/new")
        val useCase = ToggleSavedArticleUseCase(fakeRepository)

        useCase(article)

        assertTrue(fakeRepository.isArticleSaved(article))
        assertEquals(listOf(article), fakeRepository.savedArticlesFlow.value)
    }

    @Test
    fun `when toggleSavedArticleUseCase is invoked on a saved article then it removes the article`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/saved")
        fakeRepository.saveArticle(article)

        val useCase = ToggleSavedArticleUseCase(fakeRepository)

        useCase(article)

        assertFalse(fakeRepository.isArticleSaved(article))
        assertEquals(emptyList<NewsArticle>(), fakeRepository.savedArticlesFlow.value)
    }
}
