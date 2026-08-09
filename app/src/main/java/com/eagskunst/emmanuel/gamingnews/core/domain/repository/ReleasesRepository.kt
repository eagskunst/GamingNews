package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ReleasesRepository {
    fun releasesStream(forceRefresh: Boolean = false): Flow<Result<List<GameRelease>>>

    suspend fun loadNextPage(): Result<Boolean>

    val hasMorePages: StateFlow<Boolean>
}
