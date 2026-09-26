package com.eagskunst.emmanuel.gamingnews.core.domain.model

import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformSelectionReconcilerTest {

    private val retiredId = 48
    private val catalog = FakePlatformCatalog(
        listOf(
            FakePlatformCatalog.entry(igdbId = 6, name = "PC", order = 0),
            FakePlatformCatalog.entry(igdbId = 167, name = "PS5", order = 1),
            FakePlatformCatalog.entry(
                igdbId = retiredId,
                name = "PS4",
                order = 2,
                status = PlatformStatus.RETIRED
            )
        )
    )

    @Test
    fun `given an empty stored set when reconciling then the selection means All and nothing is persisted`() {
        val result = PlatformSelectionReconciler.reconcile(emptySet(), catalog)

        assertEquals(emptySet<Int>(), result.selection.selectedIds)
        assertEquals(emptySet<String>(), result.storedValue)
        assertFalse(result.requiresPersistence)
        assertNull(result.selection.notice)
    }

    @Test
    fun `given supported ids when reconciling then they are kept verbatim`() {
        val result = PlatformSelectionReconciler.reconcile(setOf("6", "167"), catalog)

        assertEquals(setOf(6, 167), result.selection.selectedIds)
        assertEquals(setOf("6", "167"), result.storedValue)
        assertFalse(result.requiresPersistence)
        assertNull(result.selection.notice)
    }

    @Test
    fun `given every supported platform selected when reconciling then the explicit set is preserved`() {
        val stored = catalog.supportedIds().map { it.toString() }.toSet()

        val result = PlatformSelectionReconciler.reconcile(stored, catalog)

        assertEquals(catalog.supportedIds(), result.selection.selectedIds)
        assertFalse("explicitly selecting all platforms must not collapse to All", result.requiresPersistence)
    }

    @Test
    fun `given malformed entries when reconciling then they are dropped and the cleaned set is persisted`() {
        val result = PlatformSelectionReconciler.reconcile(setOf("abc", "6", ""), catalog)

        assertEquals(setOf(6), result.selection.selectedIds)
        assertEquals(setOf("6"), result.storedValue)
        assertTrue(result.requiresPersistence)
    }

    @Test
    fun `given some retired ids when reconciling then they are removed remaining selections kept and a notice is raised`() {
        val result = PlatformSelectionReconciler.reconcile(setOf("6", "48"), catalog)

        assertEquals(setOf(6), result.selection.selectedIds)
        assertEquals(setOf("6"), result.storedValue)
        assertTrue(result.requiresPersistence)
        assertEquals(PlatformSelectionNotice.SOME_RETIRED_REMOVED, result.selection.notice)
    }

    @Test
    fun `given only retired ids when reconciling then the selection falls back to All with a notice`() {
        val result = PlatformSelectionReconciler.reconcile(setOf("48"), catalog)

        assertEquals(emptySet<Int>(), result.selection.selectedIds)
        assertEquals(emptySet<String>(), result.storedValue)
        assertTrue(result.requiresPersistence)
        assertEquals(PlatformSelectionNotice.ALL_RETIRED_FALLBACK, result.selection.notice)
    }

    @Test
    fun `given unrecognized ids when reconciling then they are retained as a removable fallback without notice or writes`() {
        val result = PlatformSelectionReconciler.reconcile(setOf("9999"), catalog)

        assertEquals(setOf(9999), result.selection.selectedIds)
        assertEquals(setOf(9999), result.selection.unknownIds)
        assertEquals(setOf("9999"), result.storedValue)
        assertFalse(result.requiresPersistence)
        assertNull(result.selection.notice)
    }

    @Test
    fun `given retired and unrecognized ids when reconciling then only the unrecognized survive`() {
        val result = PlatformSelectionReconciler.reconcile(setOf("48", "9999"), catalog)

        assertEquals(setOf(9999), result.selection.selectedIds)
        assertEquals(setOf(9999), result.selection.unknownIds)
        assertEquals(setOf("9999"), result.storedValue)
        assertTrue(result.requiresPersistence)
        assertEquals(PlatformSelectionNotice.SOME_RETIRED_REMOVED, result.selection.notice)
    }
}
