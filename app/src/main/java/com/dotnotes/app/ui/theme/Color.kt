package com.dotnotes.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Google Pixel / Material You Tonal Palette
// ==========================================

// Base Neutrals (Slate / Zinc Tonal)
val PixelDarkBg = Color(0xFF111316)
val PixelDarkSurface = Color(0xFF1D1F24)
val PixelDarkSurfaceContainer = Color(0xFF1D1F24)
val PixelDarkSurfaceHigh = Color(0xFF2A2D34)
val PixelDarkOutline = Color(0xFF3F424A)
val PixelDarkOutlineVariant = Color(0xFF2D3037)

val PixelLightBg = Color(0xFFF2F4F7)
val PixelLightSurface = Color(0xFFFFFFFF)
val PixelLightSurfaceContainer = Color(0xFFFFFFFF)
val PixelLightSurfaceHigh = Color(0xFFE5E8EF)
val PixelLightOutline = Color(0xFFD3D6DE)
val PixelLightOutlineVariant = Color(0xFFE2E5EC)

// Pixel Signature Accents (Obsidian / Slate)
val PixelPrimaryLight = Color(0xFF2D3139) // Slate Charcoal
val PixelPrimaryDark = Color(0xFFC4C7D0)
val PixelPrimaryContainerLight = Color(0xFFE1E3EB)
val PixelPrimaryContainerDark = Color(0xFF3B404A)

val PixelSecondaryLight = Color(0xFF4A4E57)
val PixelSecondaryDark = Color(0xFFA8ACB6)
val PixelSecondaryContainerLight = Color(0xFFECEEF4)
val PixelSecondaryContainerDark = Color(0xFF2E323A)

val PixelTertiaryLight = Color(0xFF3C4048)
val PixelTertiaryDark = Color(0xFFB0B4BD)
val PixelTertiaryContainerLight = Color(0xFFE6E8EF)
val PixelTertiaryContainerDark = Color(0xFF33373F)

// Neutral Text
val TextPrimaryLight = Color(0xFF1A1D20)
val TextSecondaryLight = Color(0xFF5E626B)
val TextTertiaryLight = Color(0xFF80848D)

val TextPrimaryDark = Color(0xFFE3E2E6)
val TextSecondaryDark = Color(0xFFC4C6D0)
val TextTertiaryDark = Color(0xFF8E9099)

// Safety & Alarm
val ErrorRed = Color(0xFFBA1A1A)
val ErrorRedDark = Color(0xFFFFB4AB)
val ErrorRedContainer = Color(0xFFFFDAD6)
val ErrorRedContainerDark = Color(0xFF93000A)
val AlarmRed = Color(0xFFFF5252)

// ==========================================
// Reminder & Alarm Badges (Material You Tonal)
// ==========================================
object ReminderBadgeColors {
    // Normal Reminder: Pixel Slate Tonal Pill
    val reminderBgLight = Color(0xFFE1E3EB)
    val reminderBorderLight = Color.Transparent
    val reminderContentLight = Color(0xFF2D3139)

    val reminderBgDark = Color(0xFF3B404A)
    val reminderBorderDark = Color.Transparent
    val reminderContentDark = Color(0xFFE1E3EB)

    // Alarm / Urgent: Pixel Coral Tonal Pill
    val alarmBgLight = Color(0xFFFCE8E6)
    val alarmBorderLight = Color.Transparent
    val alarmContentLight = Color(0xFFC5221F)

    val alarmBgDark = Color(0xFF3D1D1E)
    val alarmBorderDark = Color.Transparent
    val alarmContentDark = Color(0xFFFFB4AB)

    fun containerColor(isAlarm: Boolean, isDark: Boolean): Color {
        return if (isAlarm) {
            if (isDark) alarmBgDark else alarmBgLight
        } else {
            if (isDark) reminderBgDark else reminderBgLight
        }
    }

    fun contentColor(isAlarm: Boolean, isDark: Boolean): Color {
        return if (isAlarm) {
            if (isDark) alarmContentDark else alarmContentLight
        } else {
            if (isDark) reminderContentDark else reminderContentLight
        }
    }

    fun borderColor(isAlarm: Boolean, isDark: Boolean): Color {
        return Color.Transparent
    }
}
