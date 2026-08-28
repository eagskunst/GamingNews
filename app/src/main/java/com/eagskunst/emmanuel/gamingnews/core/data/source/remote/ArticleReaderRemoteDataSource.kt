package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class ArticleReaderRemoteDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val dispatchers: DispatcherProvider
) {

    suspend fun fetchHtml(url: String): String? = withContext(dispatchers.io) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.string()?.takeIf { it.isNotBlank() }
        } else {
            null
        }
    }
}
