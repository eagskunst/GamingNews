package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FeedProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FeedProvidersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFeedProvidersRepository : FeedProvidersRepository {

    val providersFlow = MutableStateFlow<List<FeedProvider>>(
        listOf(
            FeedProvider(
                id = "test-provider",
                name = "Test Provider",
                url = "https://example.com/feed",
                category = NewsCategory.ALL,
                enabled = true
            )
        )
    )

    override fun providersStream(category: NewsCategory?): Flow<List<FeedProvider>> =
        providersFlow.map { providers ->
            if (category != null) providers.filter { it.category == category }
            else providers
        }

    override suspend fun setProviderEnabled(id: String, enabled: Boolean) {
        providersFlow.value = providersFlow.value.map { provider ->
            if (provider.id == id) provider.copy(enabled = enabled) else provider
        }
    }

    override suspend fun restoreDefaults() {
        providersFlow.value = providersFlow.value.map { it.copy(enabled = true) }
    }
}
