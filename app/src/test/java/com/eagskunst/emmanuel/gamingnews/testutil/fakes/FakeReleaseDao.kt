package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ReleaseDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Reusable in-memory [ReleaseDao] fake, mirroring the real DAO's `releaseDate ASC` ordering and
 * REPLACE-on-conflict insert semantics.
 */
class FakeReleaseDao : ReleaseDao {

    private val releasesFlow = MutableStateFlow<List<ReleaseEntity>>(emptyList())

    override fun observeAll(): Flow<List<ReleaseEntity>> = releasesFlow

    override suspend fun clear() {
        releasesFlow.value = emptyList()
    }

    override suspend fun insertAll(releases: List<ReleaseEntity>) {
        val byId = (releasesFlow.value + releases).associateBy { it.id }
        releasesFlow.value = byId.values.sortedBy { it.releaseDate }
    }
}
