package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Snapshot test that guards provider ID stability.
 *
 * Provider IDs are persisted in DataStore as disabled-provider selections.
 * Renaming or removing an existing ID silently breaks users' saved preferences
 * (their disabled ID would no longer match any provider, re-enabling it without consent).
 *
 * When a developer adds, renames, or removes a provider, this test fails and
 * forces them to update the snapshot intentionally.
 */
@RunWith(RobolectricTestRunner::class)
class FeedProviderIdsStabilityTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `provider IDs in urls_json match the approved snapshot`() {
        val context = RuntimeEnvironment.getApplication()
        val categories = context.assets.open("urls.json").use { input ->
            json.decodeFromString<List<FeedUrlsCategoryDto>>(
                input.bufferedReader().readText()
            )
        }

        val allIds = categories.flatMap { cat ->
            (cat.allProviders + cat.sonyProviders + cat.microsoftProviders
                + cat.nintendoProviders + cat.pcProviders).map { it.id }
        }.toSortedSet()

        // This snapshot must be updated intentionally when providers are added,
        // renamed, or removed. Changing an existing ID is a breaking change
        // for users who have disabled that provider.
        val expectedIds = sortedSetOf(
            // -- es / all --
            "eurogamer-es", "vandal", "3djuegos", "vidaextra",
            "areajugones", "puregaming",
            // -- es / sony --
            "playstation-blog-es", "ign-es-ps5", "laps4",
            // -- es / microsoft --
            "xbox-news-es", "ign-es-xbox", "generacionxbox",
            // -- es / nintendo --
            "nintenderos", "ign-es-switch", "nintenduo",
            // -- es / pc --
            "ign-es-pc", "3djuegospc",
            // -- en / all --
            "vg247", "eurogamer-en", "kotaku", "vgc", "polygon",
            // -- en / sony --
            "playstation-blog-en", "pushsquare", "playstationlifestyle",
            // -- en / microsoft --
            "xbox-news-en", "purexbox", "xboxera",
            // -- en / nintendo --
            "nintendolife", "mynintendonews", "nintendowire", "kotaku-nintendo",
            // -- en / pc --
            "pcgamer", "rockpapershotgun", "pcgamesn"
        )

        assertEquals(
            "Provider IDs changed! If intentional, update the snapshot. " +
                "Renaming/removing an existing ID breaks users' saved preferences.",
            expectedIds,
            allIds
        )
    }
}
