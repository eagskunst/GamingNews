package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.IgdbAuthLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbApi
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Named

class IgdbRemoteDataSource @Inject constructor(
    private val api: IgdbApi,
    private val authLocalDataSource: IgdbAuthLocalDataSource,
    private val authRemoteDataSource: IgdbAuthRemoteDataSource,
    @Named("igdbClientId") private val clientId: String,
    private val dispatchers: DispatcherProvider
) {

    private val platforms = listOf(6, 49, 48, 130)

    suspend fun fetchUpcomingReleases(): List<IgdbReleaseDateDto> = withContext(dispatchers.io) {
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
