package com.dotnotes.app

import com.dotnotes.app.ui.theme.NoteColorThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NoteColorThemeTest {

    @Test
    fun defaultTheme_returnsNonNullTheme() {
        val lightTheme = NoteColorThemes.getThemeColors(NoteColorThemes.DEFAULT, isDark = false)
        val darkTheme = NoteColorThemes.getThemeColors(NoteColorThemes.DEFAULT, isDark = true)

        assertNotNull(lightTheme)
        assertNotNull(darkTheme)
        assertEquals(NoteColorThemes.DEFAULT, lightTheme.key)
        assertEquals(NoteColorThemes.DEFAULT, darkTheme.key)
    }

    @Test
    fun allPresetThemes_returnCorrectKeyAndNonNullColors() {
        val corePresets = listOf(
            NoteColorThemes.DEFAULT,
            NoteColorThemes.BLUE,
            NoteColorThemes.RED,
            NoteColorThemes.YELLOW,
            NoteColorThemes.GREEN
        )

        for (preset in corePresets) {
            val themeLight = NoteColorThemes.getThemeColors(preset, isDark = false)
            val themeDark = NoteColorThemes.getThemeColors(preset, isDark = true)

            assertEquals(preset, themeLight.key)
            assertEquals(preset, themeDark.key)
            assertNotNull(themeLight.background)
            assertNotNull(themeLight.swatchColor)
            assertNotNull(themeDark.background)
            assertNotNull(themeDark.swatchColor)
        }
    }

    @Test
    fun unknownThemeKey_fallsBackToDefault() {
        val unknownTheme = NoteColorThemes.getThemeColors("NON_EXISTENT_THEME", isDark = false)
        assertEquals(NoteColorThemes.DEFAULT, unknownTheme.key)
    }
}
