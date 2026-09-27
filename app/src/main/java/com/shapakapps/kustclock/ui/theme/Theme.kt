package com.shapakapps.kustclock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun KustClockTheme(
    themeColor: Color = Green,
    content: @Composable () -> Unit
) {
    val colors = darkColorScheme(
        primary = themeColor,
        onPrimary = OnPrimaryDark,
        primaryContainer = themeColor,
        onPrimaryContainer = OnPrimaryDark,
        secondary = themeColor,
        onSecondary = OnPrimaryDark,
        secondaryContainer = SurfaceVariant,
        onSecondaryContainer = TextPrimary,
        tertiary = Gold,
        onTertiary = OnPrimaryDark,
        background = Background,
        onBackground = TextPrimary,
        surface = Surface,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = TextSecondary,
        surfaceTint = Color.Transparent,
        surfaceContainer = Surface,
        surfaceContainerHigh = SurfaceVariant,
        surfaceContainerHighest = SurfaceVariant,
        surfaceContainerLow = Surface,
        surfaceContainerLowest = Surface,
        outline = Outline,
        outlineVariant = Outline,
        error = Danger,
        onError = TextPrimary
    )
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
