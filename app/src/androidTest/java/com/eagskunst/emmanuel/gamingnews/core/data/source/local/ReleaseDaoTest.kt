package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseCoverageEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ReleaseDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var releaseDao: ReleaseDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        releaseDao = database.releaseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `when_releases_are_inserted_and_observed_then_they_are_returned_ordered_by_releaseDate_ascending`() = runTest {
        val later = releaseEntity(
            id = 1L,
            releaseDate = Date(2_000L)
        )
        val earlier = releaseEntity(
            id = 2L,
            releaseDate = Date(1_000L)
        )

        releaseDao.insertAll(listOf(later, earlier))

        val observed = releaseDao.observeAll().first()

        assertEquals(2, observed.size)
        assertEquals(earlier.id, observed[0].id)
        assertEquals(later.id, observed[1].id)
    }

    @Test
    fun `when_platform_variants_of_one_game_are_inserted_then_every_record_survives`() = runTest {
        val pcRelease = releaseEntity(id = 1L, gameId = 100L, platformId = 6, releaseDate = Date(1_000L))
        val ps5Release = releaseEntity(id = 2L, gameId = 100L, platformId = 167, releaseDate = Date(5_000L))

        releaseDao.insertAll(listOf(pcRelease, ps5Release))

        val observed = releaseDao.observeAll().first()

        assertEquals(2, observed.size)
        assertEquals(setOf(6, 167), observed.map { it.platformId }.toSet())
        assertEquals(listOf(1_000L, 5_000L), observed.map { it.releaseDate.time })
    }

    @Test
    fun `when_clear_is_called_then_all_releases_are_removed`() = runTest {
        val releases = listOf(
            releaseEntity(id = 1L),
            releaseEntity(id = 2L)
        )

        releaseDao.insertAll(releases)
        releaseDao.clear()

        val observed = releaseDao.observeAll().first()

        assertTrue(observed.isEmpty())
    }

    @Test
    fun `when_a_release_is_inserted_with_the_same_id_then_the_existing_row_is_replaced`() = runTest {
        val original = releaseEntity(id = 1L, name = "Original Game")
        val updated = original.copy(name = "Updated Game")

        releaseDao.insertAll(listOf(original))
        releaseDao.insertAll(listOf(updated))

        val observed = releaseDao.observeAll().first()

        assertEquals(1, observed.size)
        assertEquals("Updated Game", observed[0].name)
    }

    @Test
    fun `when_coverage_is_upserted_then_the_latest_metadata_is_returned`() = runTest {
        assertNull(releaseDao.getCoverage())

        releaseDao.upsertCoverage(
            ReleaseCoverageEntity(coverageKey = "key-a", nextOffset = 50, isComplete = false)
        )
        releaseDao.upsertCoverage(
            ReleaseCoverageEntity(coverageKey = "key-b", nextOffset = 90, isComplete = true)
        )

        val coverage = releaseDao.getCoverage()!!
        assertEquals("key-b", coverage.coverageKey)
        assertEquals(90, coverage.nextOffset)
        assertTrue(coverage.isComplete)
    }

    @Test
    fun `when_replaceCache_is_called_then_releases_and_coverage_are_committed_together`() = runTest {
        releaseDao.insertAll(listOf(releaseEntity(id = 1L, name = "Stale Game")))
        releaseDao.upsertCoverage(
            ReleaseCoverageEntity(coverageKey = "stale", nextOffset = 50, isComplete = true)
        )
        val fresh = releaseEntity(id = 2L, name = "Fresh Game")
        val coverage = ReleaseCoverageEntity(coverageKey = "fresh", nextOffset = 1, isComplete = true)

        releaseDao.replaceCache(listOf(fresh), coverage)

        assertEquals(listOf("Fresh Game"), releaseDao.observeAll().first().map { it.name })
        assertEquals("fresh", releaseDao.getCoverage()?.coverageKey)
    }

    @Test
    fun `when_commitPage_is_called_then_existing_records_are_kept_and_coverage_advances`() = runTest {
        releaseDao.commitPage(
            listOf(releaseEntity(id = 1L, name = "Page One")),
            ReleaseCoverageEntity(coverageKey = "k", nextOffset = 50, isComplete = false)
        )
        releaseDao.commitPage(
            listOf(releaseEntity(id = 2L, name = "Page Two")),
            ReleaseCoverageEntity(coverageKey = "k", nextOffset = 51, isComplete = true)
        )

        assertEquals(
            listOf("Page One", "Page Two"),
            releaseDao.observeAll().first().map { it.name }
        )
        val coverage = releaseDao.getCoverage()!!
        assertEquals(51, coverage.nextOffset)
        assertTrue(coverage.isComplete)
    }

    private fun releaseEntity(
        id: Long,
        gameId: Long = id,
        platformId: Int = 6,
        name: String = "Game $id",
        releaseDate: Date = Date()
    ) = ReleaseEntity(
        id = id,
        gameId = gameId,
        platformId = platformId,
        name = name,
        coverUrl = null,
        releaseDate = releaseDate,
        gameUrl = null,
        fetchedAt = Date()
    )
}
