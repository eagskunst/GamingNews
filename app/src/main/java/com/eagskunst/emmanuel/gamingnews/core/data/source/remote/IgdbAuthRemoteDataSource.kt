package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.TwitchAuthApi
import javax.inject.Inject
import javax.inject.Named

class IgdbAuthRemoteDataSource @Inject constructor(
    private val api: TwitchAuthApi,
    @Named("twitchClientId") private val clientId: String,
    @Named("twitchClientSecret") private val clientSecret: String
) {

    suspend fun fetchAccessToken(): Pair<String, Long> {
        val response = api.getAccessToken(clientId, clientSecret)
        return response.accessToken to response.expiresIn
    }
}
