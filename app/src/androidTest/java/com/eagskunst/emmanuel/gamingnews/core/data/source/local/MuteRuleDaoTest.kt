package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleTabEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MuteRuleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var muteRuleDao: MuteRuleDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        muteRuleDao = database.muteRuleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `when_a_rule_with_tabs_is_inserted_then_observe_rules_returns_it_with_its_tabs`() = runTest {
        muteRuleDao.insertRule(ruleEntity("r1"))
        muteRuleDao.insertTabs(
            listOf(
                MuteRuleTabEntity(ruleId = "r1", category = "PC"),
                MuteRuleTabEntity(ruleId = "r1", category = "SONY")
            )
        )

        val observed = muteRuleDao.observeRules().first()

        assertEquals(1, observed.size)
        assertEquals("r1", observed[0].rule.id)
        assertEquals(
            setOf("PC", "SONY"),
            observed[0].tabs.mapTo(HashSet()) { it.category }
        )
    }

    @Test
    fun `when_a_rule_is_replaced_then_tabs_can_be_rewritten`() = runTest {
        muteRuleDao.insertRule(ruleEntity("r1"))
        muteRuleDao.insertTabs(listOf(MuteRuleTabEntity(ruleId = "r1", category = "PC")))

        muteRuleDao.deleteTabs("r1")
        muteRuleDao.insertRule(ruleEntity("r1", text = "updated"))
        muteRuleDao.insertTabs(listOf(MuteRuleTabEntity(ruleId = "r1", category = "NINTENDO")))

        val observed = muteRuleDao.observeRules().first()

        assertEquals(1, observed.size)
        assertEquals("updated", observed[0].rule.text)
        assertEquals(listOf("NINTENDO"), observed[0].tabs.map { it.category })
    }

    @Test
    fun `when_a_rule_is_deleted_then_its_tabs_are_cascade_deleted`() = runTest {
        muteRuleDao.insertRule(ruleEntity("r1"))
        muteRuleDao.insertTabs(listOf(MuteRuleTabEntity(ruleId = "r1", category = "PC")))

        muteRuleDao.deleteRule("r1")

        val observed = muteRuleDao.observeRules().first()
        assertTrue(observed.isEmpty())
    }

    @Test
    fun `when_rules_are_observed_then_updates_are_emitted`() = runTest {
        muteRuleDao.insertRule(ruleEntity("r1"))
        val first = muteRuleDao.observeRules().first()
        assertEquals(1, first.size)

        muteRuleDao.insertRule(ruleEntity("r2"))
        val second = muteRuleDao.observeRules().first()
        assertEquals(2, second.size)
    }

    private fun ruleEntity(id: String, text: String = "gta") = MuteRuleEntity(
        id = id,
        text = text,
        matchMode = "CONTAINS",
        caseSensitive = false,
        appliesEverywhere = false
    )
}
