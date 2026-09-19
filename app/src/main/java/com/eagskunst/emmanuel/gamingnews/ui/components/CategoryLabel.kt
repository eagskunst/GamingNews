package com.eagskunst.emmanuel.gamingnews.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsCategory

@StringRes
fun NewsCategory.labelRes(): Int = when (this) {
    NewsCategory.ALL -> R.string.tab_all_news
    NewsCategory.SONY -> R.string.tab_playstation
    NewsCategory.MICROSOFT -> R.string.tab_xbox
    NewsCategory.NINTENDO -> R.string.tab_nintendo
    NewsCategory.PC -> R.string.tab_pc
}

@Composable
fun NewsCategory.displayName(): String = stringResource(labelRes())
