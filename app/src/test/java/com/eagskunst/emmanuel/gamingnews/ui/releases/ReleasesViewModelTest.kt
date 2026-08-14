package com.eagskunst.emmanuel.gamingnews.ui.releases

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleasesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReleasesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeReleasesRepository

    private fun createViewModel(): ReleasesViewModel {
        fakeRepository = FakeReleasesRepository()
        return ReleasesViewModel(fakeRepository)
    }

    @Test
    fun `given hasMorePages stream when initialized then state reflects it and refresh is triggered`() = runTest {
        val release = Fixtures.gameRelease()
        fakeRepository = FakeReleasesRepository()
        fakeRepository.releasesResultFlow.value = Result.Success(listOf(release))
        fakeRepository.hasMorePagesFlow.value = false

        val viewModel = ReleasesViewModel(fakeRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(false, state.hasMorePages)
            assertEquals(listOf(release), state.releases)
            assertEquals(1, fakeRepository.releasesStreamInvocations)
        }
    }

    @Test
    fun `given successful result when refresh is called then releases and isLoading are updated`() = runTest {
        val release = Fixtures.gameRelease()
        val viewModel = createViewModel()
        fakeRepository.releasesResultFlow.value = Result.Success(listOf(release))

        viewModel.refresh()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf(release), state.releases)
            assertEquals(false, state.isLoading)
            assertEquals(null, state.errorMessage)
        }
    }

    @Test
    fun `given error result when refresh is called then errorMessage is updated`() = runTest {
        val exception = RuntimeException("network down")
        val viewModel = createViewModel()
        fakeRepository.releasesResultFlow.value = Result.Error(exception)

        viewModel.refresh()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(false, state.isLoading)
            assertEquals("network down", state.errorMessage)
        }
    }

    @Test
    fun `given isLoadingMore already true when loadMore is called then it is a no-op`() = runTest {
        val viewModel = createViewModel()
        fakeRepository.hasMorePagesFlow.value = true
        val pendingLoad = CompletableDeferred<Unit>()
        fakeRepository.loadNextPageBlocker = pendingLoad

        viewModel.loadMore()
        assertEquals(1, fakeRepository.loadNextPageInvocations)

        viewModel.loadMore()
        assertEquals(1, fakeRepository.loadNextPageInvocations)

        pendingLoad.complete(Unit)
    }

    @Test
    fun `given hasMorePages is false when loadMore is called then it is a no-op`() = runTest {
        val viewModel = createViewModel()
        fakeRepository.hasMorePagesFlow.value = false

        viewModel.loadMore()

        assertEquals(0, fakeRepository.loadNextPageInvocations)
    }

    @Test
    fun `given error result when loadMore is called then errorMessage is updated`() = runTest {
        val viewModel = createViewModel()
        fakeRepository.hasMorePagesFlow.value = true
        val exception = RuntimeException("page load failed")
        fakeRepository.loadNextPageResult = Result.Error(exception)

        viewModel.loadMore()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(false, state.isLoadingMore)
            assertEquals("page load failed", state.errorMessage)
        }
    }

    @Test
    fun `given a query when onSearchQueryChange is called then searchQuery is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("mario")

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("mario", state.searchQuery)
        }
    }
}
