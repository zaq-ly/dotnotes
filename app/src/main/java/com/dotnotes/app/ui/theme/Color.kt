package com.dotnotes.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Google Pixel / Material You Tonal Palette
// ==========================================

// Base Neutrals (Slate / Zinc Tonal)
val PixelDarkBg = Color(0xFF121316)
val PixelDarkSurface = Color(0xFF1E1F24)
val PixelDarkSurfaceContainer = Color(0xFF25262C)
val PixelDarkSurfaceHigh = Color(0xFF2E3036)
val PixelDarkOutline = Color(0xFF494B52)
val PixelDarkOutlineVariant = Color(0xFF383A40)

val PixelLightBg = Color(0xFFF8F9FA)
val PixelLightSurface = Color(0xFFFFFFFF)
val PixelLightSurfaceContainer = Color(0xFFEDEEF3)
val PixelLightSurfaceHigh = Color(0xFFE3E5EB)
val PixelLightOutline = Color(0xFFD0D5DD)
val PixelLightOutlineVariant = Color(0xFFE2E6EC)

// Pixel Signature Accents
val PixelPrimaryLight = Color(0xFF1A73E8) // Google Blue
val PixelPrimaryDark = Color(0xFFA8C7FA)
val PixelPrimaryContainerLight = Color(0xFFD3E3FD)
val PixelPrimaryContainerDark = Color(0xFF004A77)

val PixelSecondaryLight = Color(0xFF006A60) // Pixel Sage Teal
val PixelSecondaryDark = Color(0xFF80D5C7)
val PixelSecondaryContainerLight = Color(0xFFCCE8E2)
val PixelSecondaryContainerDark = Color(0xFF005048)

val PixelTertiaryLight = Color(0xFF6750A4) // Expressive Lilac
val PixelTertiaryDark = Color(0xFFD0BCFF)
val PixelTertiaryContainerLight = Color(0xFFEADDFF)
val PixelTertiaryContainerDark = Color(0xFF4F378B)

// Neutral Text
val TextPrimaryLight = Color(0xFF1F1F1F)
val TextSecondaryLight = Color(0xFF44474E)
val TextTertiaryLight = Color(0xFF74777F)

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
    // Normal Reminder: Pixel Sky/Cobalt Tonal Pill
    val reminderBgLight = Color(0xFFE8F0FE)
    val reminderBorderLight = Color(0xFFD2E3FC)
    val reminderContentLight = Color(0xFF1967D2)

    val reminderBgDark = Color(0xFF1A2C3D)
    val reminderBorderDark = Color(0xFF284869)
    val reminderContentDark = Color(0xFFA8C7FA)

    // Alarm / Urgent: Pixel Coral/Rose Tonal Pill
    val alarmBgLight = Color(0xFFFCE8E6)
    val alarmBorderLight = Color(0xFFFAD2CF)
    val alarmContentLight = Color(0xFFC5221F)

    val alarmBgDark = Color(0xFF3D1D1E)
    val alarmBorderDark = Color(0xFF63282B)
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
        return if (isAlarm) {
            if (isDark) alarmBorderDark else alarmBorderLight
        } else {
            if (isDark) reminderBorderDark else reminderBorderLight
        }
    }
}
