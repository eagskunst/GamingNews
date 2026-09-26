package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.UserPreferencesLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelectionNotice
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformStatus
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class DefaultUserPreferencesRepositoryTest {

    private val localDataSource: UserPreferencesLocalDataSource = mockk(relaxed = true)

    @Test
    fun `when userPreferences is accessed then it exposes the local data source flow`() {
        val expected = Fixtures.userPreferences(themeMode = ThemeMode.DARK)
        every { localDataSource.userPreferences } returns flowOf(expected)

        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        assertSame(localDataSource.userPreferences, repository.userPreferences)
    }

    @Test
    fun `when updateThemeMode is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.updateThemeMode(ThemeMode.DARK)

        coVerify { localDataSource.updateThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `when updateDynamicColor is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.updateDynamicColor(false)

        coVerify { localDataSource.updateDynamicColor(false) }
    }

    @Test
    fun `when updateDarkTheme is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.updateDarkTheme(true)

        coVerify { localDataSource.updateDarkTheme(true) }
    }

    @Test
    fun `when updateLoadImages is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.updateLoadImages(false)

        coVerify { localDataSource.updateLoadImages(false) }
    }

    @Test
    fun `when updateDailyReminder is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.updateDailyReminder(true)

        coVerify { localDataSource.updateDailyReminder(true) }
    }

    @Test
    fun `when updateDailyReminderHour is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.updateDailyReminderHour(21)

        coVerify { localDataSource.updateDailyReminderHour(21) }
    }

    @Test
    fun `when updateArticleOpenMode is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.updateArticleOpenMode(ArticleOpenMode.READER_MODE)

        coVerify { localDataSource.updateArticleOpenMode(ArticleOpenMode.READER_MODE) }
    }

    @Test
    fun `when updateReleasePlatformIds is called then ids are stored as a string set`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.updateReleasePlatformIds(setOf(6, 48))

        coVerify { localDataSource.updateReleasePlatformIdStrings(setOf("6", "48")) }
    }

    @Test
    fun `given a clean stored selection when observed then it is exposed without a write back`() = runTest {
        every { localDataSource.releasePlatformIdStrings } returns flowOf(setOf("6", "48"))
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        val selection = repository.releasePlatformSelection.first()

        assertEquals(setOf(6, 48), selection.selectedIds)
        assertEquals(emptySet<Int>(), selection.unknownIds)
        assertNull(selection.notice)
        coVerify(exactly = 0) { localDataSource.updateReleasePlatformIdStrings(any()) }
    }

    @Test
    fun `given retired and unknown stored ids when observed then reconciliation is persisted`() = runTest {
        val catalog = FakePlatformCatalog(
            listOf(
                FakePlatformCatalog.entry(igdbId = 6, name = "PC", order = 0),
                FakePlatformCatalog.entry(igdbId = 48, name = "PS4", order = 1, status = PlatformStatus.RETIRED)
            )
        )
        every { localDataSource.releasePlatformIdStrings } returns flowOf(setOf("48", "9999", "junk"))
        val repository = DefaultUserPreferencesRepository(localDataSource, catalog)

        val selection = repository.releasePlatformSelection.first()

        // The unknown id stays part of the effective selection so the user can remove it.
        assertEquals(setOf(9999), selection.selectedIds)
        assertEquals(setOf(9999), selection.unknownIds)
        // Retired id dropped and malformed value rejected; the unknown id is retained.
        coVerify { localDataSource.updateReleasePlatformIdStrings(setOf("9999")) }
    }

    @Test(expected = IllegalStateException::class)
    fun `given a failed reconciliation write when observed then the failure propagates`() = runTest {
        every { localDataSource.releasePlatformIdStrings } returns flowOf(setOf("9999", "junk"))
        coEvery { localDataSource.updateReleasePlatformIdStrings(any()) } throws
            IllegalStateException("datastore write failed")
        val repository = DefaultUserPreferencesRepository(localDataSource, FakePlatformCatalog())

        repository.releasePlatformSelection.first()
    }
}
