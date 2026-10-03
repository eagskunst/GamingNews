package com.eagskunst.emmanuel.gamingnews.utility

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShareHelperTest {

    private val context = RuntimeEnvironment.getApplication()

    private fun sendIntentFor(action: () -> Unit): Intent? {
        action()
        val chooser = shadowOf(context).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        return chooser.getParcelableExtra(Intent.EXTRA_INTENT)
    }

    @Test
    fun `given title and url when shareArticle then text includes both and brand`() {
        val sendIntent = sendIntentFor {
            context.shareArticle("Some title", "https://example.com/article")
        }

        assertEquals(Intent.ACTION_SEND, sendIntent?.action)
        assertEquals("text/plain", sendIntent?.type)
        val text = sendIntent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        assertTrue(text.contains("Some title"))
        assertTrue(text.contains("https://example.com/article"))
        assertTrue(text.contains("Gaming News"))
    }

    @Test
    fun `given no title when shareArticle then text only includes url and brand`() {
        val sendIntent = sendIntentFor {
            context.shareArticle(null, "https://example.com/article")
        }

        val text = sendIntent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        assertTrue(text.contains("https://example.com/article"))
        assertTrue(text.contains("Gaming News"))
        assertFalse(text.contains("null"))
    }
}
