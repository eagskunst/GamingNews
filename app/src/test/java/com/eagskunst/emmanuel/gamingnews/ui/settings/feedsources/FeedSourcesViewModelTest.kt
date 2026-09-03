package com.eagskunst.emmanuel.gamingnews.ui.settings.feedsources

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FeedProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetFeedProvidersUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RestoreFeedProvidersDefaultsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.SetFeedProviderEnabledUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFeedProvidersRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FeedSourcesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeFeedProvidersRepository
    private lateinit var viewModel: FeedSourcesViewModel

    @Before
    fun setUp() {
        repository = FakeFeedProvidersRepository()
        viewModel = FeedSourcesViewModel(
            getFeedProvidersUseCase = GetFeedProvidersUseCase(repository),
            setFeedProviderEnabledUseCase = SetFeedProviderEnabledUseCase(repository),
            restoreFeedProvidersDefaultsUseCase = RestoreFeedProvidersDefaultsUseCase(repository)
        )
    }

    @Test
    fun `given initialization when state is collected then default category is ALL and providers are loaded`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(NewsCategory.ALL, state.selectedCategory)
            assertFalse(state.isLoading)
            assertTrue(state.providers.isNotEmpty())
        }
    }

    @Test
    fun `given SONY category when selectCategory then selectedCategory is updated`() = runTest {
        repository.providersFlow.value = listOf(
            FeedProvider("a", "A", "https://a.com/feed", NewsCategory.ALL, enabled = true),
            FeedProvider("b", "B", "https://b.com/feed", NewsCategory.SONY, enabled = true)
        )

        viewModel.selectCategory(NewsCategory.SONY)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(NewsCategory.SONY, state.selectedCategory)
            assertEquals(1, state.providers.size)
            assertEquals("b", state.providers.first().id)
        }
    }

    @Test
    fun `given a provider when toggleProvider is called then provider enabled state changes`() = runTest {
        viewModel.toggleProvider("test-provider", false)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.providers.first { it.id == "test-provider" }.enabled)
        }
    }

    @Test
    fun `given disabled provider when restoreDefaults then provider is re-enabled`() = runTest {
        viewModel.toggleProvider("test-provider", false)
        viewModel.restoreDefaults()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state.providers.first { it.id == "test-provider" }.enabled)
        }
    }
}
