package com.example.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NovaCyan,
    onPrimary = NovaCosmicBg,
    primaryContainer = NovaCyanDark,
    onPrimaryContainer = NovaTextPrimary,
    secondary = NovaPurple,
    onSecondary = NovaCosmicBg,
    secondaryContainer = NovaPurpleDark,
    onSecondaryContainer = NovaTextPrimary,
    tertiary = NovaCyanGlow,
    background = NovaCosmicBg,
    onBackground = NovaTextPrimary,
    surface = NovaSurfaceDark,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSurfaceCard,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaSurfaceCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NovaCyanDark,
    onPrimary = NovaTextPrimary,
    primaryContainer = NovaCyan,
    onPrimaryContainer = NovaCosmicBg,
    secondary = NovaPurpleDark,
    onSecondaryContainer = NovaTextPrimary,
    background = NovaCosmicBg, // Keep Nova in sleek dark aesthetic by default
    onBackground = NovaTextPrimary,
    surface = NovaSurfaceDark,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSurfaceCard,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaSurfaceCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Nova AI sleek cosmic dark mode
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NovaCosmicBg.toArgb()
                window.navigationBarColor = NovaCosmicBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
