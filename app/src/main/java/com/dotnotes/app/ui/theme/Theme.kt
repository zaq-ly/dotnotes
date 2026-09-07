package com.dotnotes.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val DarkColorScheme = darkColorScheme(
    primary = PixelPrimaryDark,
    onPrimary = Color(0xFF14171E),
    primaryContainer = PixelPrimaryContainerDark,
    onPrimaryContainer = Color(0xFFE1E3EB),
    secondary = PixelSecondaryDark,
    onSecondary = Color(0xFF1A1D24),
    secondaryContainer = PixelSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFECEEF4),
    tertiary = PixelTertiaryDark,
    onTertiary = Color(0xFF1A1D24),
    tertiaryContainer = PixelTertiaryContainerDark,
    onTertiaryContainer = Color(0xFFE6E8EF),
    background = PixelDarkBg,
    onBackground = TextPrimaryDark,
    surface = PixelDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = PixelDarkSurfaceContainer,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = PixelDarkSurfaceContainer,
    surfaceContainerHigh = PixelDarkSurfaceHigh,
    surfaceContainerHighest = Color(0xFF35363D),
    outline = PixelDarkOutline,
    outlineVariant = PixelDarkOutlineVariant,
    inverseSurface = TextPrimaryDark,
    inverseOnSurface = PixelDarkSurface,
    inversePrimary = PixelPrimaryLight,
    error = ErrorRedDark,
    onError = Color(0xFF690005),
    errorContainer = ErrorRedContainerDark,
    onErrorContainer = ErrorRedDark
)

private val LightColorScheme = lightColorScheme(
    primary = PixelPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PixelPrimaryContainerLight,
    onPrimaryContainer = Color(0xFF14171E),
    secondary = PixelSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = PixelSecondaryContainerLight,
    onSecondaryContainer = Color(0xFF1A1D24),
    tertiary = PixelTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = PixelTertiaryContainerLight,
    onTertiaryContainer = Color(0xFF1A1D24),
    background = PixelLightBg,
    onBackground = TextPrimaryLight,
    surface = PixelLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = PixelLightSurfaceContainer,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = PixelLightSurfaceContainer,
    surfaceContainerHigh = PixelLightSurfaceHigh,
    surfaceContainerHighest = Color(0xFFDCE2EC),
    outline = PixelLightOutline,
    outlineVariant = PixelLightOutlineVariant,
    inverseSurface = PixelDarkSurface,
    inverseOnSurface = PixelLightBg,
    inversePrimary = PixelPrimaryDark,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = ErrorRed
)

@Composable
fun DotNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f
