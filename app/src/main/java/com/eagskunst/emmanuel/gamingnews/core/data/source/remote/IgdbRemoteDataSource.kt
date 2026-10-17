package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.IgdbAuthLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbApi
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReleaseDateRange
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.IgdbRejectedTokenException
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.IgdbTokenAcquisitionException
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

class IgdbRemoteDataSource @Inject constructor(
    private val api: IgdbApi,
    private val authLocalDataSource: IgdbAuthLocalDataSource,
    private val authRemoteDataSource: IgdbAuthRemoteDataSource,
    private val platformCatalog: PlatformCatalog,
    @Named("igdbClientId") private val clientId: String,
    private val dispatchers: DispatcherProvider
) {

    private val tokenMutex = Mutex()

    suspend fun fetchUpcomingReleases(
        offset: Int = 0,
        window: ReleaseDateRange
    ): List<IgdbReleaseDateDto> = withContext(dispatchers.io) {
        executeQuery(buildQuery(offset, window).toRequestBody(MEDIA_TYPE))
    }

    /**
     * Fetches exact (day-precision) release dates for a set of game IDs on supported
     * platforms, chunked so a single query never exceeds IGDB's URL/expr limits. Used by the
     * followed-games sync; a missing ID in the result means the game has no exact date left.
     */
    suspend fun fetchReleaseDatesForGames(
        gameIds: Collection<Long>
    ): List<IgdbReleaseDateDto> = withContext(dispatchers.io) {
        gameIds.toList()
            .chunked(GAME_ID_CHUNK_SIZE)
            .flatMap { chunk ->
                executeQuery(buildFollowedGamesQuery(chunk).toRequestBody(MEDIA_TYPE))
            }
    }

    /**
     * Runs [body] against the release_dates endpoint, renewing the access token once when
     * IGDB rejects it with a 401.
     */
    private suspend fun executeQuery(body: RequestBody): List<IgdbReleaseDateDto> {
        val token = getValidAccessToken()
        try {
            return requestReleases(token, body)
        } catch (exception: HttpException) {
            if (exception.code() != 401) throw exception
            val replacement = renewRejectedToken(token)
            try {
                return requestReleases(replacement, body)
            } catch (retryException: HttpException) {
                if (retryException.code() == 401) {
                    throw IgdbRejectedTokenException(retryException)
                }
                throw retryException
            }
        }
    }

    private suspend fun requestReleases(token: String, body: RequestBody): List<IgdbReleaseDateDto> =
        api.getReleaseDates(clientId, "Bearer $token", body)

    private fun buildQuery(offset: Int, window: ReleaseDateRange): String {
        val supportedIds = platformCatalog.supportedIds().sorted().joinToString(",")
        val windowStartSeconds = window.startMillis / 1000
        val windowEndSeconds = window.endMillis / 1000
        return buildString {
            appendLine("fields id,date,human,date_format,platform,game.name,game.url,game.cover.url;")
            appendLine(
                "where platform = ($supportedIds) " +
                    "& date >= $windowStartSeconds & date <= $windowEndSeconds;"
            )
            appendLine("sort date asc;")
            appendLine("limit $PAGE_LIMIT;")
            appendLine("offset $offset;")
        }
    }

    private fun buildFollowedGamesQuery(gameIds: List<Long>): String {
        val ids = gameIds.joinToString(",")
        val supportedIds = platformCatalog.supportedIds().sorted().joinToString(",")
        return buildString {
            appendLine("fields id,date,date_format,platform,game.id;")
            appendLine(
                "where game = ($ids) & platform = ($supportedIds) & date_format = 0;"
            )
            appendLine("sort date asc;")
            appendLine("limit $FOLLOWED_GAMES_LIMIT;")
        }
    }

    private suspend fun getValidAccessToken(): String = authLocalDataSource.getAccessToken(clientId)
        ?: tokenMutex.withLock {
            authLocalDataSource.getAccessToken(clientId) ?: fetchAndStoreAccessToken()
        }

    private suspend fun renewRejectedToken(rejectedToken: String): String = tokenMutex.withLock {
        val currentToken = authLocalDataSource.getAccessToken(clientId)
        if (currentToken != null && currentToken != rejectedToken) {
            currentToken
        } else {
            authLocalDataSource.invalidateAccessToken(rejectedToken, clientId)
            fetchAndStoreAccessToken()
        }
    }

    private suspend fun fetchAndStoreAccessToken(): String {
        try {
            val (token, expiresIn) = authRemoteDataSource.fetchAccessToken()
            authLocalDataSource.saveAccessToken(token, expiresIn, clientId)
            return token
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            throw IgdbTokenAcquisitionException(exception)
        }
    }

    companion object {
        private val MEDIA_TYPE = "text/plain".toMediaType()
        const val PAGE_LIMIT = 50

        /** Game IDs per query chunk; keeps the where-clause comfortably under IGDB's limits. */
        const val GAME_ID_CHUNK_SIZE = 100
        const val FOLLOWED_GAMES_LIMIT = 500
    }
}
