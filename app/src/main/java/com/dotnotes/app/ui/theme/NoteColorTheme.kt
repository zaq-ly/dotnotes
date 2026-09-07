package com.dotnotes.app.ui.theme

import androidx.compose.ui.graphics.Color

data class NoteThemeColors(
    val key: String,
    val background: Color,
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val strokeColor: Color,
    val swatchColor: Color
)

object NoteColorThemes {
    const val DEFAULT = "DEFAULT"
    const val BLUE = "BLUE"
    const val RED = "RED"
    const val YELLOW = "YELLOW"
    const val GREEN = "GREEN"

    // Backward compatibility aliases
    const val ROSE = "ROSE"
    const val PURPLE = "PURPLE"
    const val ORANGE = "ORANGE"
    const val TEAL = "TEAL"

    // 4 Pilar Warna Utama + Standar (Warna Tenang, Tidak Gonjreng)
    val allKeys = listOf(DEFAULT, BLUE, RED, YELLOW, GREEN)

    fun getThemeColors(key: String?, isDark: Boolean): NoteThemeColors {
        return when (key?.uppercase()) {
            BLUE -> if (isDark) {
                NoteThemeColors(
                    key = BLUE,
                    background = Color(0xFF1B2836),
                    surface = Color(0xFF1B2836),
                    primary = Color(0xFFA8C7FA),
                    onPrimary = Color(0xFF042B59),
                    onSurface = Color(0xFFD2E3FC),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFFA8C7FA)
                )
            } else {
                NoteThemeColors(
                    key = BLUE,
                    background = Color(0xFFE8F0FE),
                    surface = Color(0xFFE8F0FE),
                    primary = Color(0xFF1A73E8),
                    onPrimary = Color.White,
                    onSurface = Color(0xFF174EA6),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFF1A73E8)
                )
            }

            RED, ROSE -> if (isDark) {
                NoteThemeColors(
                    key = RED,
                    background = Color(0xFF351F22),
                    surface = Color(0xFF351F22),
                    primary = Color(0xFFFFB4AB),
                    onPrimary = Color(0xFF5C111C),
                    onSurface = Color(0xFFFAD2CF),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFFFFB4AB)
                )
            } else {
                NoteThemeColors(
                    key = RED,
                    background = Color(0xFFFCE8E6),
                    surface = Color(0xFFFCE8E6),
                    primary = Color(0xFFD93025),
                    onPrimary = Color.White,
                    onSurface = Color(0xFF9A1B14),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFFD93025)
                )
            }

            YELLOW, ORANGE -> if (isDark) {
                NoteThemeColors(
                    key = YELLOW,
                    background = Color(0xFF332918),
                    surface = Color(0xFF332918),
                    primary = Color(0xFFFFDF70),
                    onPrimary = Color(0xFF423400),
                    onSurface = Color(0xFFFEEFC3),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFFFFDF70)
                )
            } else {
                NoteThemeColors(
                    key = YELLOW,
                    background = Color(0xFFFEF7E0),
                    surface = Color(0xFFFEF7E0),
                    primary = Color(0xFFE37400),
                    onPrimary = Color.White,
                    onSurface = Color(0xFF7A4300),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFFE37400)
                )
            }

            GREEN, TEAL -> if (isDark) {
                NoteThemeColors(
                    key = GREEN,
                    background = Color(0xFF1B2E22),
                    surface = Color(0xFF1B2E22),
                    primary = Color(0xFFA8E3BA),
                    onPrimary = Color(0xFF0F3B20),
                    onSurface = Color(0xFFCEEAD6),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFFA8E3BA)
                )
            } else {
                NoteThemeColors(
                    key = GREEN,
                    background = Color(0xFFE6F4EA),
                    surface = Color(0xFFE6F4EA),
                    primary = Color(0xFF1E8E3E),
                    onPrimary = Color.White,
                    onSurface = Color(0xFF0D652D),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFF1E8E3E)
                )
            }

            PURPLE -> if (isDark) {
                NoteThemeColors(
                    key = PURPLE,
                    background = Color(0xFF2B1F38),
                    surface = Color(0xFF2B1F38),
                    primary = Color(0xFFD0BCFF),
                    onPrimary = Color(0xFF381E72),
                    onSurface = Color(0xFFE8D0FB),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFFD0BCFF)
                )
            } else {
                NoteThemeColors(
                    key = PURPLE,
                    background = Color(0xFFF3E8FD),
                    surface = Color(0xFFF3E8FD),
                    primary = Color(0xFF8430CE),
                    onPrimary = Color.White,
                    onSurface = Color(0xFF5B1A99),
                    strokeColor = Color.Transparent,
                    swatchColor = Color(0xFF8430CE)
                )
            }

            else -> if (isDark) {
                NoteThemeColors(
                    key = DEFAULT,
                    background = PixelDarkSurface,
                    surface = PixelDarkSurface,
                    primary = PixelPrimaryDark,
                    onPrimary = Color(0xFF14171E),
                    onSurface = TextPrimaryDark,
                    strokeColor = Color.Transparent,
                    swatchColor = PixelPrimaryDark
                )
            } else {
                NoteThemeColors(
                    key = DEFAULT,
                    background = PixelLightSurface,
                    surface = PixelLightSurface,
                    primary = PixelPrimaryLight,
                    onPrimary = Color.White,
                    onSurface = TextPrimaryLight,
                    strokeColor = Color.Transparent,
                    swatchColor = PixelPrimaryLight
                )
            }
        }
    }
}
