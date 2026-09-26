package com.eagskunst.emmanuel.gamingnews.core.domain.repository

import com.eagskunst.emmanuel.gamingnews.core.domain.model.GamePlatform
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformStatus

/**
 * Domain-facing, replaceable source of platform definitions.
 *
 * The single catalog backs filter-chip options and ordering, the platform IDs used in IGDB
 * queries, display-name lookups and saved-selection reconciliation. Implementations can be
 * swapped (e.g. for a remote catalog) without changing consumers.
 */
interface PlatformCatalog {

    /** Every known platform (supported and retired), sorted by [GamePlatform.displayOrder]. */
    val platforms: List<GamePlatform>

    /** Platforms currently offered for filtering and fetching, in display order. */
    fun supportedPlatforms(): List<GamePlatform> =
        platforms.filter { it.status == PlatformStatus.SUPPORTED }

    /** IGDB IDs of supported platforms. */
    fun supportedIds(): Set<Int> =
        supportedPlatforms().mapTo(mutableSetOf()) { it.igdbId }

    /** Finds a platform by IGDB ID, including retired entries. */
    fun find(igdbId: Int): GamePlatform? = platforms.find { it.igdbId == igdbId }

    /** Display name for [igdbId]; falls back to a generic label for unrecognized IDs. */
    fun displayName(igdbId: Int): String = find(igdbId)?.displayName ?: "Platform $igdbId"
}
