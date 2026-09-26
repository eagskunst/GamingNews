package com.eagskunst.emmanuel.gamingnews.core.domain.model

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog

/**
 * Releases-tab platform filter selection.
 *
 * [selectedIds] holds the effective stored selection; an empty set means "All platforms".
 * [unknownIds] is the subset of [selectedIds] that no catalog entry can explain — retained
 * so the user can review and remove them instead of silently widening or narrowing results.
 */
data class PlatformSelection(
    val selectedIds: Set<Int> = emptySet(),
    val unknownIds: Set<Int> = emptySet(),
    val notice: PlatformSelectionNotice? = null
)

enum class PlatformSelectionNotice {
    /** Some selected platforms retired; remaining selections were kept. */
    SOME_RETIRED_REMOVED,

    /** Every selected platform retired; the selection fell back to "All". */
    ALL_RETIRED_FALLBACK
}

data class ReconciliationResult(
    val selection: PlatformSelection,
    /** The value that should be persisted (supported + unknown IDs, malformed dropped). */
    val storedValue: Set<String>,
    /** True when [storedValue] differs from what was read and must be written back. */
    val requiresPersistence: Boolean
)

/**
 * Reconciles a persisted raw selection (string IDs) against the current platform catalog.
 *
 * - Malformed values are rejected safely (dropped).
 * - Known retired IDs are removed deliberately and reported via a notice.
 * - Unrecognized IDs are retained and surfaced as [PlatformSelection.unknownIds].
 */
object PlatformSelectionReconciler {

    fun reconcile(stored: Set<String>, catalog: PlatformCatalog): ReconciliationResult {
        val selected = linkedSetOf<Int>()
        val unknown = linkedSetOf<Int>()
        var removedRetired = false

        for (entry in stored) {
            val id = entry.toIntOrNull() ?: continue
            when {
                id in catalog.supportedIds() -> selected += id
                catalog.find(id) != null -> removedRetired = true
                else -> {
                    selected += id
                    unknown += id
                }
            }
        }

        val storedValue = (selected + unknown).mapTo(linkedSetOf()) { it.toString() }
        val notice = when {
            !removedRetired -> null
            selected.isEmpty() && unknown.isEmpty() -> PlatformSelectionNotice.ALL_RETIRED_FALLBACK
            else -> PlatformSelectionNotice.SOME_RETIRED_REMOVED
        }

        return ReconciliationResult(
            selection = PlatformSelection(
                selectedIds = selected,
                unknownIds = unknown,
                notice = notice
            ),
            storedValue = storedValue,
            requiresPersistence = storedValue != stored
        )
    }
}
