package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameDetails
import kotlinx.coroutines.flow.Flow

interface GameDetailsRepository {
    /**
     * Details for [gameId], cache first: a cached copy is emitted immediately, then a fresh
     * one when the cache is missing or stale. A failed refresh keeps the cached copy and only
     * surfaces [Result.Error] when there is nothing to show.
     */
    fun gameDetails(gameId: Long): Flow<Result<GameDetails>>
}
