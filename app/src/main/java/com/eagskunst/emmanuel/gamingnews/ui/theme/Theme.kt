package com.eagskunst.emmanuel.gamingnews.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonPrimaryDark,
    onPrimary = CrimsonOnPrimaryDark,
    primaryContainer = CrimsonPrimaryContainerDark,
    onPrimaryContainer = CrimsonOnPrimaryContainerDark,
    secondary = CrimsonSecondaryDark,
    background = CrimsonBackgroundDark,
    surface = CrimsonBackgroundDark,
    surfaceVariant = CrimsonSurfaceVariantDark,
    onSurfaceVariant = CrimsonOnSurfaceVariantDark,
    outline = CrimsonOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimaryLight,
    onPrimary = CrimsonOnPrimaryLight,
    primaryContainer = CrimsonPrimaryContainerLight,
    onPrimaryContainer = CrimsonOnPrimaryContainerLight,
    secondary = CrimsonSecondaryLight,
    background = CrimsonBackgroundLight,
    surface = CrimsonBackgroundLight,
    surfaceVariant = CrimsonSurfaceVariantLight,
    onSurfaceVariant = CrimsonOnSurfaceVariantLight,
    outline = CrimsonOutlineLight
)

@Composable
fun GamingNewsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled by default so the app keeps its red brand identity
    // instead of following the device wallpaper palette (Material You).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // `enableEdgeToEdge()` only picks the status/navigation bar icon contrast once,
    // based on the *system* dark mode at activity creation. Since this app lets users
    // pick a theme independent of the system setting, we need to keep the icon
    // appearance in sync with the actual `darkTheme` value ourselves, on every change.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
