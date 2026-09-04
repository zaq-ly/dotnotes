package com.dotnotes.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

private val DarkColorScheme = darkColorScheme(
    primary = Zinc100,
    onPrimary = Zinc950,
    primaryContainer = Zinc800,
    onPrimaryContainer = Zinc100,
    secondary = Zinc400,
    onSecondary = Zinc950,
    secondaryContainer = Zinc800,
    onSecondaryContainer = Zinc200,
    tertiary = Zinc500,
    onTertiary = Zinc950,
    tertiaryContainer = Zinc800,
    onTertiaryContainer = Zinc100,
    background = Zinc950,
    onBackground = Zinc100,
    surface = Zinc900,
    onSurface = Zinc100,
    surfaceVariant = Zinc800,
    onSurfaceVariant = Zinc400,
    surfaceContainer = Zinc900,
    surfaceContainerHigh = Zinc800,
    surfaceContainerHighest = Zinc700,
    outline = Zinc700,
    outlineVariant = Zinc800,
    inverseSurface = Zinc100,
    inverseOnSurface = Zinc950,
    inversePrimary = Zinc900,
    error = ErrorRedDark,
    onError = Zinc950,
    errorContainer = ErrorRedContainerDark,
    onErrorContainer = ErrorRedDark
)

private val LightColorScheme = lightColorScheme(
    primary = Zinc900,
    onPrimary = PureWhite,
    primaryContainer = Zinc200,
    onPrimaryContainer = Zinc900,
    secondary = Zinc600,
    onSecondary = PureWhite,
    secondaryContainer = Zinc100,
    onSecondaryContainer = Zinc900,
    tertiary = Zinc500,
    onTertiary = PureWhite,
    tertiaryContainer = Zinc200,
    onTertiaryContainer = Zinc900,
    background = Zinc50,
    onBackground = Zinc950,
    surface = PureWhite,
    onSurface = Zinc900,
    surfaceVariant = Zinc100,
    onSurfaceVariant = Zinc600,
    surfaceContainer = Zinc100,
    surfaceContainerHigh = Zinc200,
    surfaceContainerHighest = Zinc300,
    outline = Zinc300,
    outlineVariant = Zinc200,
    inverseSurface = Zinc900,
    inverseOnSurface = Zinc100,
    inversePrimary = Zinc200,
    error = ErrorRed,
    onError = PureWhite,
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
