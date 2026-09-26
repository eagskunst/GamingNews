package com.eagskunst.emmanuel.gamingnews.core.domain.model

/**
 * A catalog entry describing a release platform the app knows about.
 *
 * Entries are never deleted: when a platform is no longer offered it transitions to
 * [PlatformStatus.RETIRED] but keeps its identity and display name so previously stored
 * selections and cached records remain explainable.
 */
data class GamePlatform(
    val igdbId: Int,
    val displayName: String,
    val displayOrder: Int,
    val status: PlatformStatus = PlatformStatus.SUPPORTED
)

enum class PlatformStatus {
    SUPPORTED,
    RETIRED
}
