package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReviewEligibilityPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class ReviewCatalogDataSourceTest {

    private lateinit var dataSource: ReviewCatalogDataSource

    @Before
    fun setUp() {
        dataSource = ReviewCatalogDataSource(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `given spanish locale when selectedSource then returns eurogamer es`() {
        val source = dataSource.selectedSource(Locale("es"))

        assertNotNull(source)
        assertEquals("reviews-eurogamer-es", source?.id)
        assertEquals("es", source?.language)
        assertEquals(ReviewEligibilityPolicy.GamingFeedReviews, source?.eligibilityPolicy)
    }

    @Test
    fun `given spanish regional locale when selectedSource then returns eurogamer es`() {
        val source = dataSource.selectedSource(Locale("es", "MX"))

        assertNotNull(source)
        assertEquals("reviews-eurogamer-es", source?.id)
    }

    @Test
    fun `given english locale when selectedSource then returns eurogamer en`() {
        val source = dataSource.selectedSource(Locale.ENGLISH)

        assertNotNull(source)
        assertEquals("reviews-eurogamer-en", source?.id)
        assertEquals("en", source?.language)
        assertEquals(ReviewEligibilityPolicy.GamingFeedReviews, source?.eligibilityPolicy)
    }

    @Test
    fun `given unsupported locale when selectedSource then falls back to english`() {
        val source = dataSource.selectedSource(Locale("fr"))

        assertNotNull(source)
        assertEquals("reviews-eurogamer-en", source?.id)
    }

    @Test
    fun `when allSources then contains both configured sources with stable distinct ids`() {
        val sources = dataSource.allSources()

        assertEquals(2, sources.size)
        val ids = sources.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
