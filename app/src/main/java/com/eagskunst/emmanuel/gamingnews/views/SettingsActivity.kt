package com.eagskunst.emmanuel.gamingnews.views

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.common.ContactInfo
import com.eagskunst.emmanuel.gamingnews.ui.settings.SettingsScreen
import com.eagskunst.emmanuel.gamingnews.ui.settings.SettingsViewModel
import com.eagskunst.emmanuel.gamingnews.ui.theme.GamingNewsTheme
import com.eagskunst.emmanuel.gamingnews.utility.openCustomTab
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            GamingNewsTheme(themeMode = uiState.themeMode, dynamicColor = uiState.dynamicColor) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    onContactEmailClick = ::sendContactEmail,
                    onContactWebsiteClick = {
                        openCustomTab(getString(R.string.contact_website_url).toUri())
                    },
                    onPrivacyPolicyClick = {
                        openCustomTab(getString(R.string.privacy_policy_url).toUri())
                    }
                )
            }
        }

    }

    private fun sendContactEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:${ContactInfo.EMAIL}".toUri()
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, ContactInfo.EMAIL, Toast.LENGTH_LONG).show()
            openCustomTab(getString(R.string.contact_website_url).toUri())
        }
    }
}
