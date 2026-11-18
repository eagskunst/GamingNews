package com.eagskunst.emmanuel.gamingnews.core.data.repository

import android.content.Context
import coil3.SingletonImageLoader
import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ImageCacheRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultImageCacheRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : ImageCacheRepository {

    override suspend fun sizeBytes(): Long = withContext(dispatchers.io) {
        SingletonImageLoader.get(context).diskCache?.size ?: 0L
    }

    override suspend fun clear() = withContext(dispatchers.io) {
        val imageLoader = SingletonImageLoader.get(context)
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
        Unit
    }
}
