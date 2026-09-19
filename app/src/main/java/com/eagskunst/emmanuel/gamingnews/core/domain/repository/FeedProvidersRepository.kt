package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.domain.model.FeedProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import kotlinx.coroutines.flow.Flow

interface FeedProvidersRepository {
    fun providersStream(category: NewsCategory? = null): Flow<List<FeedProvider>>
    suspend fun setProviderEnabled(id: String, enabled: Boolean)
    suspend fun restoreDefaults()
}
