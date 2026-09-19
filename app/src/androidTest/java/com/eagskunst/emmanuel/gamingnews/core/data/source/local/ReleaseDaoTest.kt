package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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

    private fun releaseEntity(
        id: Long,
        name: String = "Game $id",
        releaseDate: Date = Date()
    ) = ReleaseEntity(
        id = id,
        name = name,
        coverUrl = null,
        releaseDate = releaseDate,
        platforms = "PC",
        gameUrl = null,
        fetchedAt = Date()
    )
}
