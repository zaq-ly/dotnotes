package com.dotnotes.app.ui.theme

import androidx.compose.ui.graphics.Color

// Monochrome Neutral & Zinc Palette
val Zinc950 = Color(0xFF09090B) // Deep background dark
val Zinc900 = Color(0xFF18181B) // Surface dark / Primary light text
val Zinc800 = Color(0xFF27272A) // Surface variant / containers dark
val Zinc700 = Color(0xFF3F3F46) // Dark border & outlines
val Zinc600 = Color(0xFF52525B) // Medium zinc / secondary light
val Zinc500 = Color(0xFF71717A) // Mid zinc / muted text
val Zinc400 = Color(0xFFA1A1AA) // Light zinc / secondary dark
val Zinc300 = Color(0xFFD4D4D8) // Light border & outlines
val Zinc200 = Color(0xFFE4E4E7) // Light containers
val Zinc100 = Color(0xFFF4F4F5) // Off-white / light surface container / dark primary
val Zinc50 = Color(0xFFFAFAFA)  // Clean light background
val PureWhite = Color(0xFFFFFFFF)

// Accent & Safety
val ErrorRed = Color(0xFFEF4444)
val ErrorRedDark = Color(0xFFF87171)
val ErrorRedContainer = Color(0xFFFEE2E2)
val ErrorRedContainerDark = Color(0xFF450A0A)
val AlarmRed = Color(0xFFFF1744)

// Professional Editorial Tones for Reminder & Alarm (Subtle, Clean, Non-AI-Slop)
object ReminderBadgeColors {
    // Normal Reminder: Refined Cobalt / Slate
    val reminderBgLight = Color(0xFFEFF6FF)
    val reminderBorderLight = Color(0xFFDBEAFE)
    val reminderContentLight = Color(0xFF1D4ED8)

    val reminderBgDark = Color(0xFF172554).copy(alpha = 0.55f)
    val reminderBorderDark = Color(0xFF1E3A8A).copy(alpha = 0.45f)
    val reminderContentDark = Color(0xFF93C5FD)

    // Alarm / Urgent / Overdue: Refined Crimson / Rose
    val alarmBgLight = Color(0xFFFFF1F2)
    val alarmBorderLight = Color(0xFFFFE4E6)
    val alarmContentLight = Color(0xFFBE123C)

    val alarmBgDark = Color(0xFF4C0519).copy(alpha = 0.55f)
    val alarmBorderDark = Color(0xFF881337).copy(alpha = 0.45f)
    val alarmContentDark = Color(0xFFFDA4AF)

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
