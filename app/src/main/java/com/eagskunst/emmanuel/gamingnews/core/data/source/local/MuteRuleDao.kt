package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleTabEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleWithTabs
import kotlinx.coroutines.flow.Flow

@Dao
interface MuteRuleDao {

    @Transaction
    @Query("SELECT * FROM mute_rules ORDER BY rowid")
    fun observeRules(): Flow<List<MuteRuleWithTabs>>

    @Transaction
    @Query("SELECT * FROM mute_rules")
    suspend fun getRules(): List<MuteRuleWithTabs>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: MuteRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTabs(tabs: List<MuteRuleTabEntity>)

    @Query("DELETE FROM mute_rule_tabs WHERE ruleId = :ruleId")
    suspend fun deleteTabs(ruleId: String)

    @Query("DELETE FROM mute_rules WHERE id = :ruleId")
    suspend fun deleteRule(ruleId: String)
}
