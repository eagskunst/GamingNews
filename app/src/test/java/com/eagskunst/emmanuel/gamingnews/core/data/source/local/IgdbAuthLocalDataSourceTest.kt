package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class IgdbAuthLocalDataSourceTest {

    private var now = 1_000_000L
    private val clientId = "client-id"
    private val dataSource = IgdbAuthLocalDataSource(RuntimeEnvironment.getApplication()) { now }

    @Before
    fun clearStoredToken() = runTest {
        dataSource.clear()
    }

    @Test
    fun `given nothing stored when getAccessToken then returns null`() = runTest {
        assertNull(dataSource.getAccessToken(clientId))
    }

    @Test
    fun `given saved token not expired when getAccessToken then returns the token`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = 3_600, clientId = clientId)

        assertEquals("some-token", dataSource.getAccessToken(clientId))
    }

    @Test
    fun `given saved token already expired when getAccessToken then returns null`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = -10, clientId = clientId)

        assertNull(dataSource.getAccessToken(clientId))
    }

    @Test
    fun `given saved token inside expiration margin when getAccessToken then returns null`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = 60, clientId = clientId)

        assertNull(dataSource.getAccessToken(clientId))
    }

    @Test
    fun `given saved token just beyond expiration margin when getAccessToken then returns token`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = 61, clientId = clientId)

        assertEquals("some-token", dataSource.getAccessToken(clientId))
    }

    @Test
    fun `given token saved for another client when getAccessToken then returns null`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = 3_600, clientId = "old-client")

        assertNull(dataSource.getAccessToken(clientId))
    }

    @Test
    fun `given saved token when clear then getAccessToken returns null`() = runTest {
        dataSource.saveAccessToken("some-token", expiresIn = 3_600, clientId = clientId)

        dataSource.clear()

        assertNull(dataSource.getAccessToken(clientId))
    }
}
