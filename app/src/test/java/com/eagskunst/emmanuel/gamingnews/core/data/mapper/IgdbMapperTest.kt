package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbCoverDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbGameDto
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbReleaseDateDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IgdbMapperTest {

    @Test
    fun `maps release date to game release`() {
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

        val release = dto.toGameRelease()

        assertEquals(456L, release?.id)
        assertEquals("Test Game", release?.name)
        assertEquals("https://images.igdb.com/t_cover_big/abc.jpg", release?.coverUrl)
        assertEquals(listOf("PS4"), release?.platforms)
        assertEquals("https://igdb.com/games/test-game", release?.gameUrl)
    }

    @Test
    fun `returns null when game has no name`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 48,
            game = IgdbGameDto(id = 456, name = null, url = null, cover = null)
        )

        assertNull(dto.toGameRelease())
    }

    @Test
    fun `ignores unknown platform ids`() {
        val dto = IgdbReleaseDateDto(
            id = 123,
            date = 1_700_000_000,
            human = "Nov 2023",
            platform = 999,
            game = IgdbGameDto(id = 1, name = "Game", url = null, cover = null)
        )

        assertEquals(emptyList<String>(), dto.toGameRelease()?.platforms)
    }
}
