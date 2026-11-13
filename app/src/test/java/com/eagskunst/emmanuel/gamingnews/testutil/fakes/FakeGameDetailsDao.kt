package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.GameDetailsDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.GameDetailsEntity

/** In-memory [GameDetailsDao] fake with the same REPLACE-on-conflict upsert as Room. */
class FakeGameDetailsDao : GameDetailsDao {

    val rows = mutableMapOf<Long, GameDetailsEntity>()

    override suspend fun get(gameId: Long): GameDetailsEntity? = rows[gameId]

    override suspend fun upsert(details: GameDetailsEntity) {
        rows[details.gameId] = details
    }

    override suspend fun deleteFetchedBefore(epochMillis: Long) {
        rows.values.removeAll { it.fetchedAt < epochMillis }
    }
}
