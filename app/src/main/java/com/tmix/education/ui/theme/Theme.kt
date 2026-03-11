package com.tmix.education.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * TMIX Education Theme - Material 3
 * Based on logo colors: Navy Blue (#1E3A5F) & Red (#E31837)
 */

private val LightColorScheme = lightColorScheme(
    primary = TMixNavy,
    onPrimary = TextOnPrimary,
    primaryContainer = TMixNavyLight,
    onPrimaryContainer = White,
    
    secondary = TMixRed,
    onSecondary = TextOnPrimary,
    secondaryContainer = TMixRedLight,
    onSecondaryContainer = White,
    
    tertiary = Info,
    
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    error = Error,
    onError = White,
    errorContainer = ErrorLight,
    onErrorContainer = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = TMixNavyLight,
    onPrimary = White,
    primaryContainer = TMixNavy,
    onPrimaryContainer = White,
    
    secondary = TMixRedLight,
    onSecondary = White,
    secondaryContainer = TMixRed,
    onSecondaryContainer = White,
    
    background = Color(0xFF0F1419),
    onBackground = White,
    surface = Color(0xFF1A1F2E),
    onSurface = White,
    surfaceVariant = Color(0xFF252B3B),
    onSurfaceVariant = Color(0xFF94A3B8),
    
    error = TMixRedLight,
    onError = White
)

@Composable
fun TMixEducationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}