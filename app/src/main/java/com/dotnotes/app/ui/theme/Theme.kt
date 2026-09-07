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
    onPrimary = Color(0xFF003258),
    primaryContainer = PixelPrimaryContainerDark,
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = PixelSecondaryDark,
    onSecondary = Color(0xFF003731),
    secondaryContainer = PixelSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFCCE8E2),
    tertiary = PixelTertiaryDark,
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = PixelTertiaryContainerDark,
    onTertiaryContainer = Color(0xFFEADDFF),
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
    onPrimaryContainer = Color(0xFF041E49),
    secondary = PixelSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = PixelSecondaryContainerLight,
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = PixelTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = PixelTertiaryContainerLight,
    onTertiaryContainer = Color(0xFF21005D),
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
