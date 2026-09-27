package com.shapakapps.kustclock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun KustClockTheme(
    themeColor: Color = Green,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkScheme(themeColor) else lightScheme(themeColor)
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

private fun darkScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = OnAccent,
    primaryContainer = accent,
    onPrimaryContainer = OnAccent,
    secondary = accent,
    onSecondary = OnAccent,
    secondaryContainer = accent,
    onSecondaryContainer = OnAccent,
    tertiary = Gold,
    onTertiary = OnAccent,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = Color.Transparent,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceVariant,
    surfaceContainerLow = DarkSurface,
    surfaceContainerLowest = DarkSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = Danger,
    onError = DarkTextPrimary
)

private fun lightScheme(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = OnAccent,
    primaryContainer = accent,
    onPrimaryContainer = OnAccent,
    secondary = accent,
    onSecondary = OnAccent,
    secondaryContainer = accent,
    onSecondaryContainer = OnAccent,
    tertiary = Gold,
    onTertiary = OnAccent,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = Color.Transparent,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = LightSurfaceVariant,
    surfaceContainerLow = LightSurface,
    surfaceContainerLowest = LightSurface,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = Danger,
    onError = Color.White
)
