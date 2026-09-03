package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class FeedProvidersLocalDataSourceTest {

    private val dataSource = FeedProvidersLocalDataSource(RuntimeEnvironment.getApplication())

    @Before
    fun clearDisabledProviders() = runTest {
        dataSource.restoreDefaults()
    }

    @Test
    fun `given nothing stored when disabledProviderIds then returns empty set`() = runTest {
        assertEquals(emptySet<String>(), dataSource.disabledProviderIds.first())
    }

    @Test
    fun `given provider disabled when disabledProviderIds then contains the disabled id`() = runTest {
        dataSource.setProviderEnabled("eurogamer-es", false)

        assertTrue(dataSource.disabledProviderIds.first().contains("eurogamer-es"))
    }

    @Test
    fun `given provider disabled then re-enabled when disabledProviderIds then does not contain the id`() = runTest {
        dataSource.setProviderEnabled("eurogamer-es", false)
        dataSource.setProviderEnabled("eurogamer-es", true)

        assertFalse(dataSource.disabledProviderIds.first().contains("eurogamer-es"))
    }

    @Test
    fun `given multiple disabled providers when restoreDefaults then disabledProviderIds is empty`() = runTest {
        dataSource.setProviderEnabled("eurogamer-es", false)
        dataSource.setProviderEnabled("vandal", false)

        dataSource.restoreDefaults()

        assertEquals(emptySet<String>(), dataSource.disabledProviderIds.first())
    }

    @Test
    fun `given multiple disabled providers when disabledProviderIds then contains all disabled ids`() = runTest {
        dataSource.setProviderEnabled("eurogamer-es", false)
        dataSource.setProviderEnabled("vandal", false)

        val disabled = dataSource.disabledProviderIds.first()
        assertEquals(setOf("eurogamer-es", "vandal"), disabled)
    }
}
