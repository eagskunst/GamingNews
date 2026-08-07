package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.IgdbAuthLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbApi
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class IgdbRemoteDataSource(
    private val api: IgdbApi,
    private val authLocalDataSource: IgdbAuthLocalDataSource,
    private val authRemoteDataSource: IgdbAuthRemoteDataSource,
    private val clientId: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val platforms = listOf(6, 49, 48, 130)

    suspend fun fetchUpcomingReleases(): List<IgdbReleaseDateDto> = withContext(dispatcher) {
        val token = getValidAccessToken()
        val authorization = "Bearer $token"
        val timestamp = System.currentTimeMillis() / 1000

        val query = buildString {
            appendLine("fields id,date,human,platform,game.name,game.url,game.cover.url;")
            appendLine("where platform = (${platforms.joinToString(",")}) & date > $timestamp;")
            appendLine("sort date asc;")
            appendLine("limit 50;")
        }

        val body = query.toRequestBody(MEDIA_TYPE)
        api.getReleaseDates(clientId, authorization, body)
    }

    private suspend fun getValidAccessToken(): String {
        return authLocalDataSource.getAccessToken()
            ?: run {
                val (token, expiresIn) = authRemoteDataSource.fetchAccessToken()
                authLocalDataSource.saveAccessToken(token, expiresIn)
                token
            }
    }

    companion object {
        private val MEDIA_TYPE = "text/plain".toMediaType()
    }
}
