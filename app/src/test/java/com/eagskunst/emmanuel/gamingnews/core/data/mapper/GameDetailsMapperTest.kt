package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbCoverDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameDetailsDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameReleaseDateDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbNamedDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbPlatformDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbWebsiteDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbWebsiteTypeDto
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameLinkType
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakePlatformCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameDetailsMapperTest {

    private val catalog = FakePlatformCatalog()
    private val pc = IgdbPlatformDto(id = 6, name = "PC (Microsoft Windows)", abbreviation = "PC")
    private val mac = IgdbPlatformDto(id = 14, name = "Mac", abbreviation = null)
    private val switch2 = IgdbPlatformDto(id = 508, name = "Nintendo Switch 2", abbreviation = "NS2")

    private fun release(platform: IgdbPlatformDto, date: Long?, format: Int? = 0, human: String? = null) =
        IgdbGameReleaseDateDto(id = 1, date = date, human = human, dateFormat = format, platform = platform)

    @Test
    fun `given a nameless game when mapped then the result is null`() {
        assertNull(IgdbGameDetailsDto(id = 1, name = " ").toGameDetails(catalog))
    }

    @Test
    fun `given regional rows for a platform when mapped then the earliest date wins`() {
        val dto = IgdbGameDetailsDto(
            id = 1,
            name = "Fable",
            releaseDates = listOf(release(pc, 2_000_000), release(pc, 1_000_000))
        )

        val releases = dto.toGameDetails(catalog)!!.platformReleases

        assertEquals(1, releases.size)
        assertEquals(1_000_000_000L, releases.single().releaseDate!!.time)
        assertTrue(releases.single().hasExactDate)
    }

    @Test
    fun `given a listed platform without a release row when mapped then it is kept undated and last`() {
        val dto = IgdbGameDetailsDto(
            id = 1,
            name = "Game",
            platforms = listOf(switch2, pc, mac),
            releaseDates = listOf(release(pc, 1_000_000), release(mac, 3_000_000, format = 2, human = "2028"))
        )

        val releases = dto.toGameDetails(catalog)!!.platformReleases

        // Catalog names win for tracked platforms; others fall back to IGDB's label.
        assertEquals(listOf("PC", "Mac", "Switch 2"), releases.map { it.platformName })
        assertFalse(releases[1].hasExactDate)
        assertEquals("2028", releases[1].human)
        assertNull(releases[2].releaseDate)
    }

    @Test
    fun `given add-ons prefixed with the game name when mapped then the prefix is trimmed`() {
        val dto = IgdbGameDetailsDto(
            id = 1,
            name = "The Witcher 3: Wild Hunt",
            expansions = listOf(IgdbNamedDto(1, "The Witcher 3: Wild Hunt - Blood and Wine")),
            dlcs = listOf(IgdbNamedDto(2, "Unrelated Pack"), IgdbNamedDto(3, "The Witcher 3: Wild Hunt"))
        )

        val details = dto.toGameDetails(catalog)!!

        assertEquals(listOf("Blood and Wine"), details.expansions)
        assertEquals(listOf("Unrelated Pack", "The Witcher 3: Wild Hunt"), details.dlcs)
        assertEquals("Shadow of the Erdtree", "Elden Ring: Shadow of the Erdtree".trimParentName("Elden Ring"))
    }

    @Test
    fun `given websites of many types when mapped then only the official site and stores remain`() {
        fun site(typeId: Int, url: String) = IgdbWebsiteDto(1, url, IgdbWebsiteTypeDto(typeId))
        val dto = IgdbGameDetailsDto(
            id = 1,
            name = "Game",
            websites = listOf(site(4, "https://facebook"), site(13, "https://steam"), site(1, "https://official"))
        )

        val links = dto.toGameDetails(catalog)!!.links

        assertEquals(listOf(GameLinkType.OFFICIAL, GameLinkType.STEAM), links.map { it.type })
    }

    @Test
    fun `given ratings and thumbnails when mapped then scores round and images are resized`() {
        val dto = IgdbGameDetailsDto(
            id = 1,
            name = "Game",
            rating = 93.77,
            ratingCount = 5526,
            aggregatedRating = 91.7,
            aggregatedRatingCount = 0,
            hypes = 0,
            cover = IgdbCoverDto(1, "//images.igdb.com/igdb/image/upload/t_thumb/co1.jpg"),
            screenshots = listOf(IgdbCoverDto(2, "//images.igdb.com/igdb/image/upload/t_thumb/sc1.jpg"))
        )

        val details = dto.toGameDetails(catalog)!!

        assertEquals(94, details.userRating!!.score)
        assertNull(details.criticRating)
        assertNull(details.hypes)
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/co1.jpg", details.coverUrl)
        assertEquals(
            listOf("https://images.igdb.com/igdb/image/upload/t_screenshot_big/sc1.jpg"),
            details.screenshots
        )
    }
}
