package com.eagskunst.emmanuel.gamingnews.core.data.repository

import android.app.Application
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.asImage
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.eagskunst.emmanuel.gamingnews.testutil.TestDispatcherProvider
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toOkioPath
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(DelicateCoilApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DefaultImageCacheRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val imageLoader = ImageLoader.Builder(context)
        .diskCache(
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("test_image_cache").toOkioPath())
                .build()
        )
        .build()
    private val repository = DefaultImageCacheRepository(context, TestDispatcherProvider())

    @Before
    fun setUp() {
        SingletonImageLoader.setUnsafe(imageLoader)
    }

    @After
    fun tearDown() {
        SingletonImageLoader.reset()
        imageLoader.shutdown()
    }

    private fun writeDiskEntry() {
        val diskCache = imageLoader.diskCache!!
        val editor = diskCache.openEditor("key")!!
        diskCache.fileSystem.write(editor.metadata) { writeUtf8("metadata") }
        diskCache.fileSystem.write(editor.data) { writeUtf8("image bytes") }
        editor.commit()
    }

    @Test
    fun `given the disk cache has an entry when sizeBytes is called then it returns the disk cache size`() = runTest {
        writeDiskEntry()

        val size = repository.sizeBytes()

        assertTrue(size > 0)
        assertEquals(imageLoader.diskCache!!.size, size)
    }

    @Test
    fun `given cached images when clear is called then disk and memory caches are emptied`() = runTest {
        writeDiskEntry()
        val memoryKey = MemoryCache.Key("key")
        imageLoader.memoryCache!![memoryKey] =
            MemoryCache.Value(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888).asImage())

        repository.clear()

        assertEquals(0L, repository.sizeBytes())
        assertNull(imageLoader.memoryCache!![memoryKey])
    }
}
