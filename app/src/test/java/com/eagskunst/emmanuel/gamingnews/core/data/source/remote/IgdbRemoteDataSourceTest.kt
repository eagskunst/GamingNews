package com.eagskunst.emmanuel.gamingnews.core.data.source.remote

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.IgdbAuthLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbApi
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IgdbRemoteDataSourceTest {

    private val testDispatchers = TestDispatcherProvider()

    private val api: IgdbApi = mockk()
    private val authLocalDataSource: IgdbAuthLocalDataSource = mockk(relaxed = true)
    private val authRemoteDataSource: IgdbAuthRemoteDataSource = mockk(relaxed = true)

    private val clientId = "test-client-id"

    private val dataSource = IgdbRemoteDataSource(
        api = api,
        authLocalDataSource = authLocalDataSource,
        authRemoteDataSource = authRemoteDataSource,
        clientId = clientId,
        dispatchers = testDispatchers
    )

    @Test
    fun `given cached access token when fetchUpcomingReleases then uses it and does not fetch a new one`() = runTest {
        val cachedToken = "cached-token"
        val clientIdSlot = slot<String>()
        val authorizationSlot = slot<String>()
        val bodySlot = slot<RequestBody>()

        coEvery { authLocalDataSource.getAccessToken() } returns cachedToken
        coEvery {
            api.getReleaseDates(
                capture(clientIdSlot),
                capture(authorizationSlot),
                capture(bodySlot)
            )
        } returns emptyList()

        dataSource.fetchUpcomingReleases(0)

        coVerify(exactly = 0) { authRemoteDataSource.fetchAccessToken() }
        coVerify(exactly = 1) { authLocalDataSource.getAccessToken() }

        assertEquals(clientId, clientIdSlot.captured)
        assertEquals("Bearer $cachedToken", authorizationSlot.captured)
        assertRequestBodyContainsQueryParts(bodySlot.captured, offset = 0)
    }

    @Test
    fun `given no cached access token when fetchUpcomingReleases then fetches saves and uses a new token`() = runTest {
        val freshToken = "fresh-token"
        val expiresIn = 3600L
        val clientIdSlot = slot<String>()
        val authorizationSlot = slot<String>()
        val bodySlot = slot<RequestBody>()

        coEvery { authLocalDataSource.getAccessToken() } returns null
        coEvery { authRemoteDataSource.fetchAccessToken() } returns (freshToken to expiresIn)
        coEvery {
            api.getReleaseDates(
                capture(clientIdSlot),
                capture(authorizationSlot),
                capture(bodySlot)
            )
        } returns emptyList()

        dataSource.fetchUpcomingReleases(0)

        coVerify(exactly = 1) { authRemoteDataSource.fetchAccessToken() }
        coVerify(exactly = 1) { authLocalDataSource.saveAccessToken(freshToken, expiresIn) }

        assertEquals(clientId, clientIdSlot.captured)
        assertEquals("Bearer $freshToken", authorizationSlot.captured)
        assertRequestBodyContainsQueryParts(bodySlot.captured, offset = 0)
    }

    @Test
    fun `when fetchUpcomingReleases then passes Client-ID and Authorization headers to api`() = runTest {
        val token = "token"
        val clientIdSlot = slot<String>()
        val authorizationSlot = slot<String>()
        val bodySlot = slot<RequestBody>()

        coEvery { authLocalDataSource.getAccessToken() } returns token
        coEvery {
            api.getReleaseDates(
                capture(clientIdSlot),
                capture(authorizationSlot),
                capture(bodySlot)
            )
        } returns listOf(mockk<IgdbReleaseDateDto>(relaxed = true))

        dataSource.fetchUpcomingReleases(42)

        assertEquals(clientId, clientIdSlot.captured)
        assertEquals("Bearer $token", authorizationSlot.captured)
        assertRequestBodyContainsQueryParts(bodySlot.captured, offset = 42)
    }

    private fun assertRequestBodyContainsQueryParts(body: RequestBody, offset: Int) {
        val text = body.bodyString()
        assertTrue(text.isNotBlank())
        assertTrue(text.contains("fields id,date,human,platform,game.name,game.url,game.cover.url"))
        assertTrue(text.contains("limit ${IgdbRemoteDataSource.PAGE_LIMIT}"))
        assertTrue(text.contains("offset $offset"))
        assertTrue(text.contains("platform = (6,49,48,130,167,169,508)"))
    }

    private fun RequestBody.bodyString(): String {
        val buffer = okio.Buffer()
        writeTo(buffer)
        return buffer.readUtf8()
    }
}
