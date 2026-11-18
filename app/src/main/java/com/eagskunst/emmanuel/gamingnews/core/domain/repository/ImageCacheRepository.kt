package com.eagskunst.emmanuel.gamingnews.core.domain.repository

interface ImageCacheRepository {
    /** Size of the on-disk image cache, in bytes. */
    suspend fun sizeBytes(): Long

    /** Deletes every cached image (disk and memory). */
    suspend fun clear()
}
