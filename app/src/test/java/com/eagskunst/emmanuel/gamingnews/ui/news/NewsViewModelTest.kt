package com.eagskunst.emmanuel.gamingnews.ui.news

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetFeedUrlsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetNewsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetSavedArticlesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetUserPreferencesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ToggleSavedArticleUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeNewsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeNewsRepository: FakeNewsRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var feedUrlsUseCase: GetFeedUrlsUseCase

    @Before
    fun setUp() {
        fakeNewsRepository = FakeNewsRepository()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
        feedUrlsUseCase = mockk()
        every { feedUrlsUseCase.invoke(any()) } returns listOf("https://example.com/feed")
    }

    private fun createViewModel(): NewsViewModel = NewsViewModel(
        getNewsUseCase = GetNewsUseCase(fakeNewsRepository),
        getSavedArticlesUseCase = GetSavedArticlesUseCase(fakeNewsRepository),
        toggleSavedArticleUseCase = ToggleSavedArticleUseCase(fakeNewsRepository),
        getFeedUrlsUseCase = feedUrlsUseCase,
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakeUserPreferencesRepository)
    )

    @Test
    fun `given saved articles when initialized then savedLinks reflects saved article links`() = runTest {
        val article = Fixtures.newsArticle(link = "https://example.com/a1")
        fakeNewsRepository.savedArticlesFlow.value = listOf(article)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(setOf("https://example.com/a1"), state.savedLinks)
        }
    }

    @Test
    fun `given preferences when initialized then loadImages is applied to state`() = runTest {
        fakeUserPreferencesRepository.preferencesFlow.value = Fixtures.userPreferences(loadImages = false)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(false, state.loadImages)
        }
    }

    @Test
    fun `given successful news result when refresh is called then articles and isLoading are updated`() = runTest {
        val article = Fixtures.newsArticle()
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(article))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf(article), state.articles)
            assertEquals(false, state.isLoading)
            assertEquals(null, state.errorMessage)
        }
    }

    @Test
    fun `given error result when refresh is called then errorMessage is updated`() = runTest {
        val exception = RuntimeException("boom")
        fakeNewsRepository.newsResultFlow.value = Result.Error(exception)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(false, state.isLoading)
            assertEquals("boom", state.errorMessage)
        }
    }

    @Test
    fun `given category when selectCategory is called then selectedCategory is updated and refresh is triggered`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectCategory(NewsCategory.SONY)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(NewsCategory.SONY, state.selectedCategory)
            assertEquals(true, fakeNewsRepository.lastForceRefresh)
        }
    }

    @Test
    fun `when refresh is called with forceRefresh true then repository receives forceRefresh true`() = runTest {
        val viewModel = createViewModel()

        viewModel.refresh(forceRefresh = true)

        assertEquals(true, fakeNewsRepository.lastForceRefresh)
    }


    @Test
    fun `given article when toggleSavedArticle is called then use case is invoked`() = runTest {
        val article = Fixtures.newsArticle()
        val viewModel = createViewModel()

        viewModel.toggleSavedArticle(article)

        assertTrue(fakeNewsRepository.savedArticlesFlow.value.contains(article))
    }

    @Test
    fun `given a query when onSearchQueryChange is called then searchQuery is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("zelda")

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("zelda", state.searchQuery)
        }
    }

    @Test
    fun `given new articles when refresh is called with notifyNewArticles then newArticlesCount reflects added articles`() = runTest {
        val existingArticle = Fixtures.newsArticle(link = "https://example.com/existing")
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(existingArticle))
        val viewModel = createViewModel()

        val newArticle = Fixtures.newsArticle(link = "https://example.com/new")
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(newArticle, existingArticle))
        viewModel.refresh(forceRefresh = true, notifyNewArticles = true)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(1, state.newArticlesCount)
        }
    }

    @Test
    fun `given no new articles when refresh is called with notifyNewArticles then newArticlesCount is null`() = runTest {
        val existingArticle = Fixtures.newsArticle(link = "https://example.com/existing")
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(existingArticle))
        val viewModel = createViewModel()

        viewModel.refresh(forceRefresh = true, notifyNewArticles = true)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(null, state.newArticlesCount)
        }
    }

    @Test
    fun `given new articles when refresh is called without notifyNewArticles then newArticlesCount stays null`() = runTest {
        val existingArticle = Fixtures.newsArticle(link = "https://example.com/existing")
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(existingArticle))
        val viewModel = createViewModel()

        val newArticle = Fixtures.newsArticle(link = "https://example.com/new")
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(newArticle, existingArticle))
        viewModel.selectCategory(NewsCategory.SONY)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(null, state.newArticlesCount)
        }
    }

    @Test
    fun `given a shown banner when dismissNewArticlesBanner is called then newArticlesCount is cleared`() = runTest {
        val existingArticle = Fixtures.newsArticle(link = "https://example.com/existing")
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(existingArticle))
        val viewModel = createViewModel()

        val newArticle = Fixtures.newsArticle(link = "https://example.com/new")
        fakeNewsRepository.newsResultFlow.value = Result.Success(listOf(newArticle, existingArticle))
        viewModel.refresh(forceRefresh = true, notifyNewArticles = true)

        viewModel.dismissNewArticlesBanner()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(null, state.newArticlesCount)
        }
    }
}
