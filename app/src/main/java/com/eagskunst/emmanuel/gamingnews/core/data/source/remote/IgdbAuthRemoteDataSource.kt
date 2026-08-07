package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.TwitchAuthApi

class IgdbAuthRemoteDataSource(
    private val api: TwitchAuthApi,
    private val clientId: String,
    private val clientSecret: String
) {

    suspend fun fetchAccessToken(): Pair<String, Long> {
        val response = api.getAccessToken(clientId, clientSecret)
        return response.accessToken to response.expiresIn
    }
}
