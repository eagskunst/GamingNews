package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.model.GamePlatform
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformStatus
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog

/**
 * Reusable [PlatformCatalog] fake over an explicit entry list, so tests can model catalog
 * evolution (additions, retirements, renames, reorders) without touching the bundled catalog.
 */
class FakePlatformCatalog(
    entries: List<GamePlatform> = DEFAULT_ENTRIES
) : PlatformCatalog {

    override val platforms: List<GamePlatform> = entries.sortedBy { it.displayOrder }

    companion object {
        val DEFAULT_ENTRIES = listOf(
            GamePlatform(igdbId = 167, displayName = "PS5", displayOrder = 0),
            GamePlatform(igdbId = 508, displayName = "Switch 2", displayOrder = 1),
            GamePlatform(igdbId = 169, displayName = "Xbox Series X|S", displayOrder = 2),
            GamePlatform(igdbId = 6, displayName = "PC", displayOrder = 3),
            GamePlatform(igdbId = 130, displayName = "Nintendo Switch", displayOrder = 4),
            GamePlatform(igdbId = 48, displayName = "PS4", displayOrder = 5),
            GamePlatform(igdbId = 49, displayName = "Xbox One", displayOrder = 6)
        )

        fun entry(
            igdbId: Int,
            name: String = "Platform $igdbId",
            order: Int = igdbId,
            status: PlatformStatus = PlatformStatus.SUPPORTED
        ) = GamePlatform(igdbId = igdbId, displayName = name, displayOrder = order, status = status)
    }
}
