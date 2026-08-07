package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import kotlinx.coroutines.flow.Flow

interface ReleasesRepository {
    fun releasesStream(forceRefresh: Boolean = false): Flow<Result<List<GameRelease>>>
}
