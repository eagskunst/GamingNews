package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameDetails
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.GameDetailsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** [GameDetailsRepository] fake that replays [result] for every game; reassign to change it. */
class FakeGameDetailsRepository(
    var result: Result<GameDetails>
) : GameDetailsRepository {
    override fun gameDetails(gameId: Long): Flow<Result<GameDetails>> = flowOf(result)
}
