package com.eagskunst.emmanuel.gamingnews.core.data.repository

import android.content.Context
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.FeedProvidersLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.FeedProviderDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.FeedUrlsCategoryDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FeedProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.FeedProvidersRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.Locale
import javax.inject.Inject

class DefaultFeedProvidersRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localDataSource: FeedProvidersLocalDataSource
) : FeedProvidersRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val catalog by lazy { loadCatalog() }

    override fun providersStream(category: NewsCategory?): Flow<List<FeedProvider>> =
        localDataSource.disabledProviderIds.map { disabledIds ->
            val allProviders = buildProviderList(category)
            allProviders.map { provider ->
                provider.copy(enabled = provider.id !in disabledIds)
            }
        }

    override suspend fun setProviderEnabled(id: String, enabled: Boolean) {
        localDataSource.setProviderEnabled(id, enabled)
    }

    override suspend fun restoreDefaults() {
        localDataSource.restoreDefaults()
    }

    private fun buildProviderList(category: NewsCategory?): List<FeedProvider> {
        val locale = Locale.getDefault().language
        val matching = catalog.find { it.language == locale }
            ?: catalog.find { it.language == "en" }
            ?: return emptyList()

        return if (category != null) {
            providersForCategory(matching, category)
        } else {
            NewsCategory.entries.flatMap { cat -> providersForCategory(matching, cat) }
        }
    }

    private fun providersForCategory(
        dto: FeedUrlsCategoryDto,
        category: NewsCategory
    ): List<FeedProvider> {
        val providers = when (category) {
            NewsCategory.ALL -> dto.allProviders
            NewsCategory.SONY -> dto.sonyProviders
            NewsCategory.MICROSOFT -> dto.microsoftProviders
            NewsCategory.NINTENDO -> dto.nintendoProviders
            NewsCategory.PC -> dto.pcProviders
        }
        return providers.map { it.toDomain(category) }
    }

    private fun FeedProviderDto.toDomain(category: NewsCategory) = FeedProvider(
        id = id,
        name = name,
        url = url,
        category = category
    )

    private fun loadCatalog(): List<FeedUrlsCategoryDto> {
        return context.assets.open("urls.json").use { input ->
            json.decodeFromString<List<FeedUrlsCategoryDto>>(
                input.bufferedReader().use { it.readText() }
            )
        }
    }
}
