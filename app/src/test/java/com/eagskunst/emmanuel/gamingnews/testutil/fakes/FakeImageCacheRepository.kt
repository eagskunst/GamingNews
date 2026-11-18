package com.eagskunst.emmanuel.gamingnews.testutil.fakes

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ImageCacheRepository

/** Reusable in-memory [ImageCacheRepository] fake; clearing drops [currentSizeBytes] to 0. */
class FakeImageCacheRepository(
    var currentSizeBytes: Long = 0L
) : ImageCacheRepository {

    var clearCount = 0
        private set

    override suspend fun sizeBytes(): Long = currentSizeBytes

    override suspend fun clear() {
        clearCount++
        currentSizeBytes = 0L
    }
}
