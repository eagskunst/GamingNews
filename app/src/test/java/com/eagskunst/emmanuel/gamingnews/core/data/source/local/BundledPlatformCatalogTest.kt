package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledPlatformCatalogTest {

    private val catalog = BundledPlatformCatalog()

    @Test
    fun `given the bundled catalog when reading platforms then every igdb id is unique`() {
        val ids = catalog.platforms.map { it.igdbId }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `given the bundled catalog when reading platforms then they are sorted by display order`() {
        val orders = catalog.platforms.map { it.displayOrder }

        assertEquals(orders.sorted(), orders)
    }

    @Test
    fun `given the bundled catalog then the expected seven platforms are supported`() {
        val expected = mapOf(
            6 to "PC",
            48 to "PS4",
            167 to "PS5",
            49 to "Xbox One",
            169 to "Xbox Series X|S",
            130 to "Nintendo Switch",
            508 to "Switch 2"
        )

        val supported = catalog.supportedPlatforms()

        assertEquals(expected.keys, supported.map { it.igdbId }.toSet())
        expected.forEach { (id, name) -> assertEquals(name, catalog.find(id)?.displayName) }
    }

    @Test
    fun `given a retired entry when querying the catalog then it is not supported but keeps its identity`() {
        val retiredCatalog = com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog(
            listOf(
                com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog.entry(
                    igdbId = 6,
                    name = "PC",
                    order = 0
                ),
                com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog.entry(
                    igdbId = 48,
                    name = "PS4",
                    order = 1,
                    status = PlatformStatus.RETIRED
                )
            )
        )

        assertEquals(setOf(6), retiredCatalog.supportedIds())
        assertEquals("PS4", retiredCatalog.displayName(48))
        assertEquals(PlatformStatus.RETIRED, retiredCatalog.find(48)?.status)
    }

    @Test
    fun `given an unknown igdb id when resolving its display name then a label fallback is returned`() {
        assertEquals("Platform 9999", catalog.displayName(9999))
        assertNull(catalog.find(9999))
    }
}
