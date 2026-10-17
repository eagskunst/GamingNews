package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val TEST_DB = "migration-test"

@RunWith(JUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_preserves_existing_records_and_defaults_author_to_null() {
        val now = Date().time
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO articles (link, title, description, imageUrl, publicationDate, sourceName, savedAt)
                VALUES ('https://example.com/article', 'Title', 'Description', NULL, $now, 'Source', $now)
                """.trimIndent()
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT author FROM articles WHERE link = 'https://example.com/article'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertNull(cursor.getString(cursor.getColumnIndexOrThrow("author")))
        }

        db.query("SELECT COUNT(*) FROM articles").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    @Test
    fun migrate2To3_creates_mute_tables_and_preserves_articles_and_releases() {
        val now = Date().time
        helper.createDatabase(TEST_DB, 2).use { db ->
            db.execSQL(
                """
                INSERT INTO articles (link, title, description, imageUrl, publicationDate, sourceName, savedAt, author)
                VALUES ('https://example.com/article', 'Title', 'Description', NULL, $now, 'Source', $now, NULL)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO releases (id, name, coverUrl, releaseDate, platforms, gameUrl, fetchedAt)
                VALUES (1, 'Game', NULL, $now, 'PC', NULL, $now)
                """.trimIndent()
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        db.query("SELECT COUNT(*) FROM articles").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM releases").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM mute_rules").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM mute_rule_tabs").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun migrate3To4_rebuilds_the_release_cache_and_preserves_articles_and_mute_rules() {
        val now = Date().time
        helper.createDatabase(TEST_DB, 3).use { db ->
            db.execSQL(
                """
                INSERT INTO articles (link, title, description, imageUrl, publicationDate, sourceName, savedAt, author)
                VALUES ('https://example.com/article', 'Title', 'Description', NULL, $now, 'Source', $now, 'Author')
                """.trimIndent()
            )
            // Old rows were merged by game id and can't be split back into per-platform
            // records, so they must be discarded rather than migrated.
            db.execSQL(
                """
                INSERT INTO releases (id, name, coverUrl, releaseDate, platforms, gameUrl, fetchedAt)
                VALUES (1, 'Merged Game', NULL, $now, 'PC, PS5', NULL, $now)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO mute_rules (id, text, matchMode, caseSensitive, appliesEverywhere)
                VALUES ('rule-1', 'spoiler', 'CONTAINS', 0, 1)
                """.trimIndent()
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

        db.query("SELECT COUNT(*) FROM articles").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM mute_rules").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM releases").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM release_coverage").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun migrate4To5_defaults_existing_releases_to_exact_and_adds_nullable_human() {
        val now = Date().time
        helper.createDatabase(TEST_DB, 4).use { db ->
            db.execSQL(
                """
                INSERT INTO releases (id, gameId, platformId, name, coverUrl, releaseDate, gameUrl, fetchedAt)
                VALUES (1, 10, 6, 'Exact Game', NULL, $now, NULL, $now)
                """.trimIndent()
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        db.query("SELECT hasExactDate, human FROM releases WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("hasExactDate")))
            assertNull(cursor.getString(cursor.getColumnIndexOrThrow("human")))
        }
    }

    @Test
    fun migrate5To6_creates_the_followed_games_table() {
        helper.createDatabase(TEST_DB, 5).use { db ->
            db.execSQL(
                """
                INSERT INTO releases (id, gameId, platformId, name, coverUrl, releaseDate, gameUrl, hasExactDate, human, fetchedAt)
                VALUES (1, 10, 6, 'Game', NULL, 1700000000000, NULL, 1, NULL, 1700000000000)
                """.trimIndent()
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        db.query("SELECT COUNT(*) FROM followed_games").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.execSQL(
            """
            INSERT INTO followed_games (gameId, name, coverUrl, gameUrl, releaseEpochDay, offsets, notifiedOffsets, platformsLabel, followedAt, lastSyncedAt)
            VALUES (10, 'Game', NULL, NULL, 20000, 7, 0, 'PC', 1700000000000, NULL)
            """.trimIndent()
        )
        db.query("SELECT offsets, platformsLabel FROM followed_games WHERE gameId = 10").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(7, cursor.getInt(cursor.getColumnIndexOrThrow("offsets")))
            assertEquals("PC", cursor.getString(cursor.getColumnIndexOrThrow("platformsLabel")))
        }
    }

    @Test
    fun migrate1To3_applies_the_full_migration_chain() {
        val now = Date().time
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO articles (link, title, description, imageUrl, publicationDate, sourceName, savedAt)
                VALUES ('https://example.com/article', 'Title', 'Description', NULL, $now, 'Source', $now)
                """.trimIndent()
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3)

        db.query("SELECT COUNT(*) FROM articles").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM mute_rules").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }
}
