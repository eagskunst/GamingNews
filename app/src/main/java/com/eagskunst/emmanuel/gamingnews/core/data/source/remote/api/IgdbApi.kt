package com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface IgdbApi {

    @POST("release_dates")
    @Headers("Accept: application/json")
    suspend fun getReleaseDates(
        @Header("Client-ID") clientId: String,
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): List<IgdbReleaseDateDto>
}

@Serializable
data class IgdbReleaseDateDto(
    val id: Long,
    val date: Long?,
    val human: String?,
    val platform: Int,
    @SerialName("game") val game: IgdbGameDto?
)

@Serializable
data class IgdbGameDto(
    val id: Long,
    val name: String?,
    val url: String?,
    val cover: IgdbCoverDto?
)

@Serializable
data class IgdbCoverDto(
    val id: Long,
    val url: String?
)
