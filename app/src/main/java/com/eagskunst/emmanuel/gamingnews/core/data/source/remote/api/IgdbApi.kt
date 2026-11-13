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

    @POST("games")
    @Headers("Accept: application/json")
    suspend fun getGames(
        @Header("Client-ID") clientId: String,
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): List<IgdbGameDetailsDto>
}

@Serializable
data class IgdbReleaseDateDto(
    val id: Long,
    val date: Long? = null,
    val human: String? = null,
    val platform: Int,
    /** IGDB date-format id; [IGDB_DATE_FORMAT_YYYYMMDD] means a full day-precision date. */
    @SerialName("date_format") val dateFormat: Int? = null,
    @SerialName("game") val game: IgdbGameDto? = null
)

/** IGDB `date_format` id for full `YYYYMMDD` dates; anything else is approximate. */
const val IGDB_DATE_FORMAT_YYYYMMDD = 0

@Serializable
data class IgdbGameDto(
    val id: Long,
    val name: String? = null,
    val url: String? = null,
    val cover: IgdbCoverDto? = null
)

@Serializable
data class IgdbCoverDto(
    val id: Long,
    val url: String? = null
)

/**
 * One game with every reference the detail page shows expanded in a single query. Lists
 * default to empty because IGDB omits fields a game has no data for.
 */
@Serializable
data class IgdbGameDetailsDto(
    val id: Long,
    val name: String? = null,
    val summary: String? = null,
    val url: String? = null,
    val rating: Double? = null,
    @SerialName("rating_count") val ratingCount: Int? = null,
    @SerialName("aggregated_rating") val aggregatedRating: Double? = null,
    @SerialName("aggregated_rating_count") val aggregatedRatingCount: Int? = null,
    val hypes: Int? = null,
    val cover: IgdbCoverDto? = null,
    val genres: List<IgdbNamedDto> = emptyList(),
    @SerialName("game_modes") val gameModes: List<IgdbNamedDto> = emptyList(),
    val franchises: List<IgdbNamedDto> = emptyList(),
    val platforms: List<IgdbPlatformDto> = emptyList(),
    @SerialName("involved_companies") val involvedCompanies: List<IgdbInvolvedCompanyDto> = emptyList(),
    @SerialName("age_ratings") val ageRatings: List<IgdbAgeRatingDto> = emptyList(),
    @SerialName("release_dates") val releaseDates: List<IgdbGameReleaseDateDto> = emptyList(),
    val screenshots: List<IgdbCoverDto> = emptyList(),
    val videos: List<IgdbVideoDto> = emptyList(),
    @SerialName("similar_games") val similarGames: List<IgdbGameDto> = emptyList(),
    val expansions: List<IgdbNamedDto> = emptyList(),
    val dlcs: List<IgdbNamedDto> = emptyList(),
    val websites: List<IgdbWebsiteDto> = emptyList()
)

@Serializable
data class IgdbNamedDto(val id: Long, val name: String? = null)

@Serializable
data class IgdbPlatformDto(
    val id: Int,
    val name: String? = null,
    val abbreviation: String? = null
)

@Serializable
data class IgdbInvolvedCompanyDto(
    val id: Long,
    val company: IgdbNamedDto? = null,
    val developer: Boolean = false,
    val publisher: Boolean = false
)

@Serializable
data class IgdbAgeRatingDto(
    val id: Long,
    val organization: IgdbNamedDto? = null,
    @SerialName("rating_category") val ratingCategory: IgdbRatingCategoryDto? = null
)

@Serializable
data class IgdbRatingCategoryDto(val id: Long, val rating: String? = null)

/** A release-date row nested in a game; unlike [IgdbReleaseDateDto] its platform is expanded. */
@Serializable
data class IgdbGameReleaseDateDto(
    val id: Long,
    val date: Long? = null,
    val human: String? = null,
    @SerialName("date_format") val dateFormat: Int? = null,
    val platform: IgdbPlatformDto? = null
)

@Serializable
data class IgdbVideoDto(
    val id: Long,
    val name: String? = null,
    @SerialName("video_id") val videoId: String? = null
)

@Serializable
data class IgdbWebsiteDto(
    val id: Long,
    val url: String? = null,
    val type: IgdbWebsiteTypeDto? = null
)

@Serializable
data class IgdbWebsiteTypeDto(val id: Int, val type: String? = null)
