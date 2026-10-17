package com.eagskunst.emmanuel.gamingnews.ui.releases

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.FollowGameUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetReleasesUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ObserveFollowedGameIdsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UnfollowGameUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFollowedGamesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleasesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.worker.GameReleaseScheduler
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Permission-dependent bell flows; the SDK level must actually enforce POST_NOTIFICATIONS. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class ReleasesViewModelBellPermissionTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private val catalog = FakePlatformCatalog()

    private fun viewModel(
        context: Application,
        followedGames: FakeFollowedGamesRepository = FakeFollowedGamesRepository(),
        scheduler: GameReleaseScheduler = mockk(relaxed = true)
    ): ReleasesViewModel {
        val releases = FakeReleasesRepository()
        return ReleasesViewModel(
            context = context,
            getReleases = GetReleasesUseCase(releases, catalog),
            releasesRepository = releases,
            userPreferencesRepository = FakeUserPreferencesRepository(catalog = catalog),
            observeFollowedGameIds = ObserveFollowedGameIdsUseCase(followedGames),
            followGame = FollowGameUseCase(followedGames, scheduler),
            unfollowGame = UnfollowGameUseCase(followedGames, scheduler),
            catalog = catalog
        )
    }

    @Test
    fun `given permission denied when the bell is tapped then the permission request is emitted`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = viewModel(context)
        mainDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEvent.test {
            viewModel.onBellClick(Fixtures.gameRelease(id = 42))

            assertEquals(
                ReleasesUiEvent.RequestNotificationPermission,
                awaitItem()
            )
            assertNull(viewModel.uiState.value.offsetPickerFor)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given permission granted when the bell is tapped then the picker opens`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val viewModel = viewModel(context)
        mainDispatcher.scheduler.advanceUntilIdle()
        val release = Fixtures.gameRelease(id = 42)

        viewModel.onBellClick(release)

        assertEquals(release, viewModel.uiState.value.offsetPickerFor)
    }

    @Test
    fun `given a pending follow when permission is denied then a denial event is emitted`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = viewModel(context)
        mainDispatcher.scheduler.advanceUntilIdle()
        viewModel.onBellClick(Fixtures.gameRelease(id = 42))

        viewModel.uiEvent.test {
            viewModel.onPermissionResult(granted = false)

            assertEquals(ReleasesUiEvent.ShowPermissionDenied, awaitItem())
            assertNull(viewModel.uiState.value.offsetPickerFor)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a pending follow when permission is granted then the picker opens`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = viewModel(context)
        mainDispatcher.scheduler.advanceUntilIdle()
        val release = Fixtures.gameRelease(id = 42)
        viewModel.onBellClick(release)

        viewModel.onPermissionResult(granted = true)

        assertEquals(release, viewModel.uiState.value.offsetPickerFor)
    }

    @Test
    fun `given permission granted when a game is followed then the check is scheduled`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val scheduler: GameReleaseScheduler = mockk(relaxed = true)
        val followedGames = FakeFollowedGamesRepository()
        val viewModel = viewModel(context, followedGames, scheduler)
        mainDispatcher.scheduler.advanceUntilIdle()

        viewModel.onBellClick(Fixtures.gameRelease(id = 42))
        viewModel.onOffsetsConfirmed(setOf(com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset.RELEASE_DAY))
        mainDispatcher.scheduler.advanceUntilIdle()

        io.mockk.verify { scheduler.ensureScheduled() }
        assertTrue(followedGames.getAll().isNotEmpty())
    }
}
