package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.FollowedGameEntity
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.model.toMask
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
class FollowedGameDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FollowedGameDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.followedGameDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun when_games_are_followed_then_observe_ids_emits_their_game_ids() = runTest {
        dao.upsert(followedGame(gameId = 10))
        dao.upsert(followedGame(gameId = 20))

        assertEquals(setOf(10L, 20L), dao.observeIds().first().toSet())
    }

    @Test
    fun when_a_follow_is_upserted_with_the_same_game_then_the_row_is_replaced() = runTest {
        dao.upsert(followedGame(gameId = 10, name = "Old"))
        dao.upsert(followedGame(gameId = 10, name = "New", releaseEpochDay = 20_010))

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("New", all.single().name)
        assertEquals(20_010L, all.single().releaseEpochDay)
    }

    @Test
    fun when_a_follow_is_deleted_then_it_no_longer_is_returned() = runTest {
        dao.upsert(followedGame(gameId = 10))
        dao.delete(10)

        assertNull(dao.get(10))
        assertTrue(dao.observeIds().first().isEmpty())
    }

    @Test
    fun when_pruning_then_only_rows_released_before_the_day_are_removed() = runTest {
        dao.upsert(followedGame(gameId = 10, releaseEpochDay = 19_999))
        dao.upsert(followedGame(gameId = 20, releaseEpochDay = 20_000))
        dao.upsert(followedGame(gameId = 30, releaseEpochDay = 20_001))

        dao.deleteReleasedBefore(20_000)

        assertEquals(setOf(20L, 30L), dao.getAll().map { it.gameId }.toSet())
    }

    @Test
    fun when_update_sync_runs_then_date_mask_and_synced_at_are_committed() = runTest {
        dao.upsert(followedGame(gameId = 10, notifiedOffsets = setOf(ReminderOffset.RELEASE_DAY)))
        val syncedAt = Date(1_700_000_000_000L)

        dao.updateSync(gameId = 10, releaseEpochDay = 20_005, notifiedOffsets = 0, lastSyncedAt = syncedAt)

        val row = dao.get(10)!!
        assertEquals(20_005L, row.releaseEpochDay)
        assertEquals(0, row.notifiedOffsets)
        assertEquals(syncedAt, row.lastSyncedAt)
    }

    @Test
    fun when_mark_notified_runs_then_the_bit_is_merged_into_the_mask() = runTest {
        dao.upsert(followedGame(gameId = 10, notifiedOffsets = setOf(ReminderOffset.RELEASE_DAY)))

        dao.markNotified(10, ReminderOffset.ONE_DAY_BEFORE.bit)

        assertEquals(
            setOf(ReminderOffset.RELEASE_DAY, ReminderOffset.ONE_DAY_BEFORE).toMask(),
            dao.get(10)!!.notifiedOffsets
        )
    }

    private fun followedGame(
        gameId: Long,
        name: String = "Game $gameId",
        releaseEpochDay: Long = 20_000,
        notifiedOffsets: Set<ReminderOffset> = emptySet()
    ) = FollowedGameEntity(
        gameId = gameId,
        name = name,
        coverUrl = null,
        gameUrl = null,
        releaseEpochDay = releaseEpochDay,
        offsets = setOf(ReminderOffset.RELEASE_DAY).toMask(),
        notifiedOffsets = notifiedOffsets.toMask(),
        platformsLabel = "PC",
        followedAt = Date(),
        lastSyncedAt = null
    )
}
