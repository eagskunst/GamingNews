package com.eagskunst.emmanuel.gamingnews.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.eagskunst.emmanuel.gamingnews.ui.settings.SettingsScreen
import com.eagskunst.emmanuel.gamingnews.ui.settings.SettingsViewModel
import com.eagskunst.emmanuel.gamingnews.ui.theme.GamingNewsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GamingNewsTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
