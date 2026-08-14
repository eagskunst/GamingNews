package com.eagskunst.emmanuel.gamingnews.ui.reader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.ui.theme.GamingNewsTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReaderActivity : ComponentActivity() {

    private val viewModel: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val url = intent.getStringExtra(EXTRA_URL) ?: finish().let { return }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.LIGHT -> false
                com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode.DARK -> true
            }
            GamingNewsTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                ReaderScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDark,
                    onBackClick = { finish() }
                )
            }
        }

    }

    companion object {
        const val EXTRA_URL = "extra_url"

        fun newIntent(context: Context, url: String): Intent {
            return Intent(context, ReaderActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }
    }
}
