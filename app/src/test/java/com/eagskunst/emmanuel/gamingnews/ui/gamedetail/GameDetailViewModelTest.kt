package com.eagskunst.emmanuel.gamingnews.ui.gamedetail

import android.Manifest
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameDetails
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.FollowGameUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.ObserveFollowedGameIdsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.UnfollowGameUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeFollowedGamesRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeGameDetailsRepository
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeUserPreferencesRepository
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesUiEvent
import com.eagskunst.emmanuel.gamingnews.worker.GameReleaseScheduler
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class GameDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val context: Application = ApplicationProvider.getApplicationContext()
    private val today = LocalDate.now().toEpochDay()
    private val followedGames = FakeFollowedGamesRepository()
    private val scheduler: GameReleaseScheduler = mockk(relaxed = true)

    private fun upcomingGame() = Fixtures.gameDetails(
        gameId = GAME_ID,
        platformReleases = listOf(Fixtures.platformRelease(epochDay = today + 7))
    )

    private fun viewModel(
        result: Result<GameDetails> = Result.Success(upcomingGame()),
        repository: FakeGameDetailsRepository = FakeGameDetailsRepository(result)
    ) = GameDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(GameDetailViewModel.GAME_ID_ARG to GAME_ID)),
        context = context,
        repository = repository,
        userPreferencesRepository = FakeUserPreferencesRepository(
            initial = Fixtures.userPreferences(loadImages = false)
        ),
        observeFollowedGameIds = ObserveFollowedGameIdsUseCase(followedGames),
        followGame = FollowGameUseCase(followedGames, scheduler),
        unfollowGame = UnfollowGameUseCase(followedGames, scheduler),
        catalog = FakePlatformCatalog()
    )

    @Test
    fun `given an upcoming game when loaded then details the image preference and a followable release are exposed`() {
        val state = viewModel().uiState.value

        assertEquals("Some Game", state.details?.name)
        assertFalse(state.isLoading)
        assertFalse(state.loadImages)
        assertEquals(GAME_ID, state.pendingRelease?.id)
    }

    @Test
    fun `given a released game when loaded then there is nothing to follow`() {
        val released = Fixtures.gameDetails(
            gameId = GAME_ID,
            platformReleases = listOf(Fixtures.platformRelease(epochDay = today - 1))
        )

        val viewModel = viewModel(Result.Success(released))
        viewModel.onFollowClick()

        assertNull(viewModel.uiState.value.pendingRelease)
        assertFalse(viewModel.uiState.value.showOffsetPicker)
    }

    @Test
    fun `given a failed load when retry succeeds then the error clears`() {
        val repository = FakeGameDetailsRepository(Result.Error(IOException("offline")))
        val viewModel = viewModel(repository = repository)
        assertTrue(viewModel.uiState.value.hasError)

        repository.result = Result.Success(upcomingGame())
        viewModel.retry()

        assertFalse(viewModel.uiState.value.hasError)
        assertEquals("Some Game", viewModel.uiState.value.details?.name)
    }

    @Test
    fun `given notification permission when follow is confirmed then the game is followed`() = runTest {
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val viewModel = viewModel()

        viewModel.onFollowClick()
        assertTrue(viewModel.uiState.value.showOffsetPicker)
        viewModel.onOffsetsConfirmed(setOf(ReminderOffset.RELEASE_DAY))

        assertFalse(viewModel.uiState.value.showOffsetPicker)
        assertTrue(viewModel.uiState.value.isFollowed)
        assertEquals(today + 7, followedGames.getAll().single().releaseEpochDay)
    }

    @Test
    fun `given no notification permission when follow is tapped then permission is requested first`() = runTest {
        val viewModel = viewModel()

        viewModel.uiEvent.test {
            viewModel.onFollowClick()
            assertEquals(ReleasesUiEvent.RequestNotificationPermission, awaitItem())
            assertFalse(viewModel.uiState.value.showOffsetPicker)

            viewModel.onPermissionResult(granted = true)
            assertTrue(viewModel.uiState.value.showOffsetPicker)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a followed game when follow is tapped then it is unfollowed with an undo event`() = runTest {
        followedGames.follow(Fixtures.followedGame(gameId = GAME_ID))
        val viewModel = viewModel()

        viewModel.uiEvent.test {
            viewModel.onFollowClick()

            val event = awaitItem() as ReleasesUiEvent.ShowReminderRemoved
            assertFalse(viewModel.uiState.value.isFollowed)

            viewModel.undoUnfollow(event.game)
            assertTrue(viewModel.uiState.value.isFollowed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val GAME_ID = 42L
    }
}
