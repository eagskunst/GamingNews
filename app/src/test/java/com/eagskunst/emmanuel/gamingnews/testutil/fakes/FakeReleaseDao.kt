package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ReleaseDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseCoverageEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Reusable in-memory [ReleaseDao] fake, mirroring the real DAO's `releaseDate ASC` ordering,
 * REPLACE-on-conflict insert semantics and coverage metadata bookkeeping.
 */
class FakeReleaseDao : ReleaseDao {

    private val releasesFlow = MutableStateFlow<List<ReleaseEntity>>(emptyList())
    private val coverageFlow = MutableStateFlow<ReleaseCoverageEntity?>(null)

    override fun observeAll(): Flow<List<ReleaseEntity>> = releasesFlow

    override suspend fun clear() {
        releasesFlow.value = emptyList()
    }

    override suspend fun insertAll(releases: List<ReleaseEntity>) {
        val byId = (releasesFlow.value + releases).associateBy { it.id }
        releasesFlow.value = byId.values.sortedBy { it.releaseDate }
    }

    override suspend fun getCoverage(): ReleaseCoverageEntity? = coverageFlow.value

    override suspend fun upsertCoverage(coverage: ReleaseCoverageEntity) {
        coverageFlow.value = coverage
    }

    override suspend fun replaceCache(releases: List<ReleaseEntity>, coverage: ReleaseCoverageEntity) {
        clear()
        insertAll(releases)
        upsertCoverage(coverage)
    }

    override suspend fun commitPage(releases: List<ReleaseEntity>, coverage: ReleaseCoverageEntity) {
        insertAll(releases)
        upsertCoverage(coverage)
    }
}
