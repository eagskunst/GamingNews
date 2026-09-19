package com.eagskunst.emmanuel.gamingnews.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.AppDatabase
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteMatchMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.DuplicateMuteRuleException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultMuteRulesRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: DefaultMuteRulesRepository

    private val dispatchers = object : DispatcherProvider {
        override val io = Dispatchers.IO
        override val default = Dispatchers.Default
        override val main = Dispatchers.Main
    }

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = DefaultMuteRulesRepository(database, database.muteRuleDao(), dispatchers)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `when_a_rule_is_saved_then_it_is_observed_with_its_scope`() = runTest {
        repository.upsertRule(
            rule(id = "r1", scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC, NewsCategory.SONY)))
        )

        val rules = repository.observeRules().first()

        assertEquals(1, rules.size)
        assertEquals("r1", rules[0].id)
        assertEquals(MuteScope.SelectedTabs(setOf(NewsCategory.PC, NewsCategory.SONY)), rules[0].scope)
    }

    @Test
    fun `when_a_rule_is_updated_then_its_tabs_are_replaced_atomically`() = runTest {
        repository.upsertRule(rule(id = "r1", scope = MuteScope.SelectedTabs(setOf(NewsCategory.PC))))
        repository.upsertRule(rule(id = "r1", text = "zelda", scope = MuteScope.Everywhere))

        val rules = repository.observeRules().first()

        assertEquals(1, rules.size)
        assertEquals("zelda", rules[0].text)
        assertEquals(MuteScope.Everywhere, rules[0].scope)
    }

    @Test
    fun `when_a_duplicate_is_saved_then_it_throws_and_the_existing_rule_is_unchanged`() = runTest {
        repository.upsertRule(rule(id = "r1", text = "GTA"))

        assertThrows(DuplicateMuteRuleException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.upsertRule(rule(id = "r2", text = "gta"))
            }
        }

        val rules = repository.observeRules().first()
        assertEquals(1, rules.size)
        assertEquals("r1", rules[0].id)
        assertEquals("GTA", rules[0].text)
    }

    @Test
    fun `when_rules_differ_in_case_sensitivity_then_they_coexist`() = runTest {
        repository.upsertRule(rule(id = "r1", text = "GTA", caseSensitive = true))
        repository.upsertRule(rule(id = "r2", text = "gta"))

        assertEquals(2, repository.observeRules().first().size)
    }

    @Test
    fun `when_a_rule_is_deleted_then_it_is_removed`() = runTest {
        repository.upsertRule(rule(id = "r1"))
        repository.deleteRule("r1")

        assertTrue(repository.observeRules().first().isEmpty())
    }

    @Test
    fun `when_rules_are_observed_then_later_writes_emit_updates`() = runTest {
        repository.upsertRule(rule(id = "r1"))
        assertEquals(1, repository.observeRules().first().size)

        repository.upsertRule(rule(id = "r2", text = "zelda"))
        val rules = repository.observeRules().first()
        assertEquals(2, rules.size)
    }

    private fun rule(
        id: String,
        text: String = "gta",
        caseSensitive: Boolean = false,
        scope: MuteScope = MuteScope.Everywhere
    ) = MuteRule(
        id = id,
        text = text,
        matchMode = MuteMatchMode.CONTAINS,
        caseSensitive = caseSensitive,
        scope = scope
    )
}
