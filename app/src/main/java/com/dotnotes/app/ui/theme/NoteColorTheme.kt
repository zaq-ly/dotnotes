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
                    background = Color(0xFF131D2E),
                    surface = Color(0xFF1D283E),
                    primary = Color(0xFFA8C7FA),
                    onPrimary = Color(0xFF042B59),
                    onSurface = Color(0xFFE1EDFF),
                    strokeColor = Color(0xFF2C4166),
                    swatchColor = Color(0xFFA8C7FA)
                )
            } else {
                NoteThemeColors(
                    key = BLUE,
                    background = Color(0xFFEEF4FF),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFF1A73E8),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF101C30),
                    strokeColor = Color(0xFFD3E3FD),
                    swatchColor = Color(0xFF1A73E8)
                )
            }

            RED, ROSE -> if (isDark) {
                NoteThemeColors(
                    key = RED,
                    background = Color(0xFF2B1618),
                    surface = Color(0xFF3B1E22),
                    primary = Color(0xFFFFB3B9),
                    onPrimary = Color(0xFF5C111C),
                    onSurface = Color(0xFFFFECEE),
                    strokeColor = Color(0xFF5E2B31),
                    swatchColor = Color(0xFFFFB3B9)
                )
            } else {
                NoteThemeColors(
                    key = RED,
                    background = Color(0xFFFFF0F1),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFFD93025),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF330B11),
                    strokeColor = Color(0xFFFAD2CF),
                    swatchColor = Color(0xFFD93025)
                )
            }

            YELLOW, ORANGE -> if (isDark) {
                NoteThemeColors(
                    key = YELLOW,
                    background = Color(0xFF272111),
                    surface = Color(0xFF382F18),
                    primary = Color(0xFFFFDF70),
                    onPrimary = Color(0xFF423400),
                    onSurface = Color(0xFFFFF6D6),
                    strokeColor = Color(0xFF594B26),
                    swatchColor = Color(0xFFFFDF70)
                )
            } else {
                NoteThemeColors(
                    key = YELLOW,
                    background = Color(0xFFFFF9E6),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFFEA8600),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF302200),
                    strokeColor = Color(0xFFFEE7AC),
                    swatchColor = Color(0xFFEA8600)
                )
            }

            GREEN, TEAL -> if (isDark) {
                NoteThemeColors(
                    key = GREEN,
                    background = Color(0xFF14241B),
                    surface = Color(0xFF1C3326),
                    primary = Color(0xFFA8E3BA),
                    onPrimary = Color(0xFF0F3B20),
                    onSurface = Color(0xFFE2F9EB),
                    strokeColor = Color(0xFF2D543F),
                    swatchColor = Color(0xFFA8E3BA)
                )
            } else {
                NoteThemeColors(
                    key = GREEN,
                    background = Color(0xFFF0F9F3),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFF188038),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF0A2914),
                    strokeColor = Color(0xFFCEEAD6),
                    swatchColor = Color(0xFF188038)
                )
            }

            PURPLE -> if (isDark) {
                NoteThemeColors(
                    key = PURPLE,
                    background = Color(0xFF22172F),
                    surface = Color(0xFF302242),
                    primary = Color(0xFFD0BCFF),
                    onPrimary = Color(0xFF381E72),
                    onSurface = Color(0xFFF2ECFE),
                    strokeColor = Color(0xFF4C3667),
                    swatchColor = Color(0xFFD0BCFF)
                )
            } else {
                NoteThemeColors(
                    key = PURPLE,
                    background = Color(0xFFF6F0FF),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFF8430CE),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF240A42),
                    strokeColor = Color(0xFFE8D5FD),
                    swatchColor = Color(0xFF8430CE)
                )
            }

            else -> if (isDark) {
                NoteThemeColors(
                    key = DEFAULT,
                    background = PixelDarkBg,
                    surface = PixelDarkSurface,
                    primary = PixelPrimaryDark,
                    onPrimary = Color(0xFF003258),
                    onSurface = TextPrimaryDark,
                    strokeColor = PixelDarkOutlineVariant,
                    swatchColor = PixelDarkOutline
                )
            } else {
                NoteThemeColors(
                    key = DEFAULT,
                    background = PixelLightBg,
                    surface = PixelLightSurface,
                    primary = PixelPrimaryLight,
                    onPrimary = Color.White,
                    onSurface = TextPrimaryLight,
                    strokeColor = PixelLightOutlineVariant,
                    swatchColor = PixelLightOutline
                )
            }
        }
    }
}
