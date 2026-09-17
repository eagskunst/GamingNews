package com.eagskunst.emmanuel.gamingnews.core.data.mapper

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleTabEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleWithTabs
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteMatchMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory

fun MuteRuleWithTabs.toDomain(): MuteRule = MuteRule(
    id = rule.id,
    text = rule.text,
    matchMode = rule.matchMode.toMatchMode(),
    caseSensitive = rule.caseSensitive,
    scope = if (rule.appliesEverywhere || tabs.isEmpty()) {
        MuteScope.Everywhere
    } else {
        MuteScope.SelectedTabs(tabs.mapTo(linkedSetOf()) { it.category.toNewsCategory() })
    }
)

fun MuteRule.toEntity(): MuteRuleEntity = MuteRuleEntity(
    id = id,
    text = text,
    matchMode = matchMode.name,
    caseSensitive = caseSensitive,
    appliesEverywhere = scope is MuteScope.Everywhere
)

fun MuteRule.toTabEntities(): List<MuteRuleTabEntity> = when (val s = scope) {
    MuteScope.Everywhere -> emptyList()
    is MuteScope.SelectedTabs -> s.tabs.map { MuteRuleTabEntity(ruleId = id, category = it.name) }
}

private fun String.toMatchMode(): MuteMatchMode =
    MuteMatchMode.entries.find { it.name == this } ?: MuteMatchMode.CONTAINS

private fun String.toNewsCategory(): NewsCategory =
    NewsCategory.entries.find { it.name == this } ?: NewsCategory.ALL
