package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeReleasesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetReleasesUseCaseTest {

    private val fakeRepository = FakeReleasesRepository()

    @Test
    fun `given forceRefresh is true when invoke is called then it passes flag and returns emissions`() = runTest {
        val expectedReleases = listOf(
            Fixtures.gameRelease(name = "Game A"),
            Fixtures.gameRelease(name = "Game B")
        )
        fakeRepository.releasesResultFlow.value = Result.Success(expectedReleases)

        val useCase = GetReleasesUseCase(fakeRepository)
        val result = useCase(forceRefresh = true).first()

        assertTrue(fakeRepository.lastForceRefresh)
        assertEquals(Result.Success(expectedReleases), result)
    }

    @Test
    fun `given forceRefresh is false by default when invoke is called then it passes flag and returns emissions`() = runTest {
        val expectedReleases = listOf(Fixtures.gameRelease(name = "Game C"))
        fakeRepository.releasesResultFlow.value = Result.Success(expectedReleases)

        val useCase = GetReleasesUseCase(fakeRepository)
        val result = useCase().first()

        assertFalse(fakeRepository.lastForceRefresh)
        assertEquals(Result.Success(expectedReleases), result)
    }
}
