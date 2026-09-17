package com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "mute_rules")
data class MuteRuleEntity(
    @PrimaryKey val id: String,
    val text: String,
    val matchMode: String,
    val caseSensitive: Boolean,
    val appliesEverywhere: Boolean
)

@Entity(
    tableName = "mute_rule_tabs",
    primaryKeys = ["ruleId", "category"],
    foreignKeys = [
        ForeignKey(
            entity = MuteRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ruleId")]
)
data class MuteRuleTabEntity(
    val ruleId: String,
    val category: String
)

data class MuteRuleWithTabs(
    @Embedded val rule: MuteRuleEntity,
    @Relation(parentColumn = "id", entityColumn = "ruleId")
    val tabs: List<MuteRuleTabEntity>
)
