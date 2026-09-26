package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbCoverDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class IgdbMapperTest {

    @Test
    fun `when the dto has all fields then every id and the release date are preserved`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 48,
            game = IgdbGameDto(
                id = 456,
                name = "Test Game",
                url = "https://igdb.com/games/test-game",
                cover = IgdbCoverDto(id = 1, url = "//images.igdb.com/t_thumb/abc.jpg")
            )
        )

        val record = dto.toReleaseRecord()

        assertEquals(123L, record?.releaseId)
        assertEquals(456L, record?.gameId)
        assertEquals(48, record?.platformId)
        assertEquals(Date(1_700_000_000L * 1000L), record?.releaseDate)
        assertEquals("Test Game", record?.name)
        assertEquals("https://images.igdb.com/t_cover_big/abc.jpg", record?.coverUrl)
        assertEquals("https://igdb.com/games/test-game", record?.gameUrl)
    }

    @Test
    fun `when the game is null then it returns null`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 48,
            game = null
        )

        assertNull(dto.toReleaseRecord())
    }

    @Test
    fun `when the game has no name then it returns null`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 48,
            game = IgdbGameDto(id = 456, name = null, url = null, cover = null)
        )

        assertNull(dto.toReleaseRecord())
    }

    @Test
    fun `when the platform id is unknown then the record keeps the raw platform id`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 999,
            game = IgdbGameDto(id = 1, name = "Game", url = null, cover = null)
        )

        assertEquals(999, dto.toReleaseRecord()?.platformId)
    }

    @Test
    fun `when the game name is blank then it returns null`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 48,
            game = IgdbGameDto(id = 456, name = "   ", url = null, cover = null)
        )

        assertNull(dto.toReleaseRecord())
    }

    @Test
    fun `when the release date is null then it returns null`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = null,
            human = "Nov 2023",
            platform = 48,
            game = IgdbGameDto(id = 456, name = "Test Game", url = null, cover = null)
        )

        assertNull(dto.toReleaseRecord())
    }

    @Test
    fun `when the cover is null then the cover url is null`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 48,
            game = IgdbGameDto(id = 456, name = "Test Game", url = null, cover = null)
        )

        assertNull(dto.toReleaseRecord()?.coverUrl)
    }

    @Test
    fun `when the cover url is null then the returned cover url is null`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 48,
            game = IgdbGameDto(id = 456, name = "Test Game", url = null, cover = IgdbCoverDto(id = 1, url = null))
        )

        assertNull(dto.toReleaseRecord()?.coverUrl)
    }
}
