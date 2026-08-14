package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.UserPreferencesLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

class DefaultUserPreferencesRepositoryTest {

    private val localDataSource: UserPreferencesLocalDataSource = mockk(relaxed = true)

    @Test
    fun `when userPreferences is accessed then it exposes the local data source flow`() {
        val expected = Fixtures.userPreferences(themeMode = ThemeMode.DARK)
        every { localDataSource.userPreferences } returns flowOf(expected)

        val repository = DefaultUserPreferencesRepository(localDataSource)

        assertSame(localDataSource.userPreferences, repository.userPreferences)
    }

    @Test
    fun `when updateThemeMode is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource)

        repository.updateThemeMode(ThemeMode.DARK)

        coVerify { localDataSource.updateThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `when updateDynamicColor is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource)

        repository.updateDynamicColor(false)

        coVerify { localDataSource.updateDynamicColor(false) }
    }

    @Test
    fun `when updateDarkTheme is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource)

        repository.updateDarkTheme(true)

        coVerify { localDataSource.updateDarkTheme(true) }
    }

    @Test
    fun `when updateLoadImages is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource)

        repository.updateLoadImages(false)

        coVerify { localDataSource.updateLoadImages(false) }
    }

    @Test
    fun `when updateDailyReminder is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource)

        repository.updateDailyReminder(true)

        coVerify { localDataSource.updateDailyReminder(true) }
    }

    @Test
    fun `when updateArticleOpenMode is called then it delegates the same value to the local data source`() = runTest {
        val repository = DefaultUserPreferencesRepository(localDataSource)

        repository.updateArticleOpenMode(ArticleOpenMode.READER_MODE)

        coVerify { localDataSource.updateArticleOpenMode(ArticleOpenMode.READER_MODE) }
    }
}
