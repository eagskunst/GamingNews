package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import com.eagskunst.emmanuel.gamingnews.core.domain.model.GamePlatform
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformStatus
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.PlatformCatalog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bundled [PlatformCatalog] implementation.
 *
 * Adding a platform means appending an entry here; retiring one means switching its status to
 * [PlatformStatus.RETIRED]. Neither change requires schema or preference migrations.
 */
@Singleton
class BundledPlatformCatalog @Inject constructor() : PlatformCatalog {

    override val platforms: List<GamePlatform> = listOf(
        GamePlatform(igdbId = 167, displayName = "PS5", displayOrder = 0),
        GamePlatform(igdbId = 508, displayName = "Switch 2", displayOrder = 1),
        GamePlatform(igdbId = 169, displayName = "Xbox Series X|S", displayOrder = 2),
        GamePlatform(igdbId = 6, displayName = "PC", displayOrder = 3),
        GamePlatform(igdbId = 130, displayName = "Nintendo Switch", displayOrder = 4),
        GamePlatform(igdbId = 48, displayName = "PS4", displayOrder = 5),
        GamePlatform(igdbId = 49, displayName = "Xbox One", displayOrder = 6)
    )
}
