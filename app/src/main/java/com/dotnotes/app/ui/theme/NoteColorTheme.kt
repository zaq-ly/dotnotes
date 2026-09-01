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
    const val GREEN = "GREEN"
    const val YELLOW = "YELLOW"
    const val PURPLE = "PURPLE"
    const val ROSE = "ROSE"
    const val ORANGE = "ORANGE"
    const val TEAL = "TEAL"

    val allKeys = listOf(DEFAULT, BLUE, GREEN, YELLOW, PURPLE, ROSE, ORANGE, TEAL)

    fun getThemeColors(key: String?, isDark: Boolean): NoteThemeColors {
        return when (key?.uppercase()) {
            BLUE -> if (isDark) {
                NoteThemeColors(
                    key = BLUE,
                    background = Color(0xFF0B1528),
                    surface = Color(0xFF13203C),
                    primary = Color(0xFF38BDF8),
                    onPrimary = Color(0xFF082F49),
                    onSurface = Color(0xFFE0F2FE),
                    strokeColor = Color(0xFF0284C7),
                    swatchColor = Color(0xFF38BDF8)
                )
            } else {
                NoteThemeColors(
                    key = BLUE,
                    background = Color(0xFFF0F7FF),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFF0284C7),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF0F172A),
                    strokeColor = Color(0xFF0284C7),
                    swatchColor = Color(0xFF0284C7)
                )
            }

            GREEN -> if (isDark) {
                NoteThemeColors(
                    key = GREEN,
                    background = Color(0xFF071E14),
                    surface = Color(0xFF0E2E20),
                    primary = Color(0xFF34D399),
                    onPrimary = Color(0xFF022C1A),
                    onSurface = Color(0xFFD1FAE5),
                    strokeColor = Color(0xFF059669),
                    swatchColor = Color(0xFF34D399)
                )
            } else {
                NoteThemeColors(
                    key = GREEN,
                    background = Color(0xFFF0FDF4),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFF059669),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF062817),
                    strokeColor = Color(0xFF059669),
                    swatchColor = Color(0xFF059669)
                )
            }

            YELLOW -> if (isDark) {
                NoteThemeColors(
                    key = YELLOW,
                    background = Color(0xFF1F1706),
                    surface = Color(0xFF30240B),
                    primary = Color(0xFFFBBF24),
                    onPrimary = Color(0xFF361E02),
                    onSurface = Color(0xFFFEF3C7),
                    strokeColor = Color(0xFFD97706),
                    swatchColor = Color(0xFFFBBF24)
                )
            } else {
                NoteThemeColors(
                    key = YELLOW,
                    background = Color(0xFFFFFBEB),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFFD97706),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF281C06),
                    strokeColor = Color(0xFFD97706),
                    swatchColor = Color(0xFFD97706)
                )
            }

            PURPLE -> if (isDark) {
                NoteThemeColors(
                    key = PURPLE,
                    background = Color(0xFF180F29),
                    surface = Color(0xFF251840),
                    primary = Color(0xFFA78BFA),
                    onPrimary = Color(0xFF240E4A),
                    onSurface = Color(0xFFEDE9FE),
                    strokeColor = Color(0xFF7C3AED),
                    swatchColor = Color(0xFFA78BFA)
                )
            } else {
                NoteThemeColors(
                    key = PURPLE,
                    background = Color(0xFFFAF5FF),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFF7C3AED),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF1E1035),
                    strokeColor = Color(0xFF7C3AED),
                    swatchColor = Color(0xFF7C3AED)
                )
            }

            ROSE -> if (isDark) {
                NoteThemeColors(
                    key = ROSE,
                    background = Color(0xFF220B13),
                    surface = Color(0xFF351320),
                    primary = Color(0xFFFB7185),
                    onPrimary = Color(0xFF3A0617),
                    onSurface = Color(0xFFFFE4E6),
                    strokeColor = Color(0xFFE11D48),
                    swatchColor = Color(0xFFFB7185)
                )
            } else {
                NoteThemeColors(
                    key = ROSE,
                    background = Color(0xFFFFF1F2),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFFE11D48),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF2A0C16),
                    strokeColor = Color(0xFFE11D48),
                    swatchColor = Color(0xFFE11D48)
                )
            }

            ORANGE -> if (isDark) {
                NoteThemeColors(
                    key = ORANGE,
                    background = Color(0xFF221105),
                    surface = Color(0xFF361C0A),
                    primary = Color(0xFFFB923C),
                    onPrimary = Color(0xFF3A1202),
                    onSurface = Color(0xFFFFEDD5),
                    strokeColor = Color(0xFFEA580C),
                    swatchColor = Color(0xFFFB923C)
                )
            } else {
                NoteThemeColors(
                    key = ORANGE,
                    background = Color(0xFFFFF7ED),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFFEA580C),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF2B1405),
                    strokeColor = Color(0xFFEA580C),
                    swatchColor = Color(0xFFEA580C)
                )
            }

            TEAL -> if (isDark) {
                NoteThemeColors(
                    key = TEAL,
                    background = Color(0xFF071C1E),
                    surface = Color(0xFF0E2D30),
                    primary = Color(0xFF2DD4BF),
                    onPrimary = Color(0xFF02292A),
                    onSurface = Color(0xFFCCFBF1),
                    strokeColor = Color(0xFF0D9488),
                    swatchColor = Color(0xFF2DD4BF)
                )
            } else {
                NoteThemeColors(
                    key = TEAL,
                    background = Color(0xFFF0FDFA),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFF0D9488),
                    onPrimary = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF062426),
                    strokeColor = Color(0xFF0D9488),
                    swatchColor = Color(0xFF0D9488)
                )
            }

            else -> if (isDark) {
                NoteThemeColors(
                    key = DEFAULT,
                    background = Zinc950,
                    surface = Zinc900,
                    primary = Zinc100,
                    onPrimary = Zinc950,
                    onSurface = Zinc100,
                    strokeColor = Color.Transparent,
                    swatchColor = Zinc500
                )
            } else {
                NoteThemeColors(
                    key = DEFAULT,
                    background = Zinc50,
                    surface = PureWhite,
                    primary = Zinc900,
                    onPrimary = PureWhite,
                    onSurface = Zinc900,
                    strokeColor = Color.Transparent,
                    swatchColor = Zinc400
                )
            }
        }
    }
}
