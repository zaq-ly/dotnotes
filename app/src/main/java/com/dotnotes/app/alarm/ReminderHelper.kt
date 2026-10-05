package com.dotnotes.app.alarm

import com.dotnotes.app.ui.i18n.AppStrings
import java.util.Calendar

object ReminderHelper {
    const val REPEAT_NONE = "NONE"
    const val REPEAT_DAILY = "DAILY"
    const val REPEAT_WEEKLY = "WEEKLY"
    const val REPEAT_MONTHLY = "MONTHLY"
    const val REPEAT_YEARLY = "YEARLY"
    const val REPEAT_DAYS_PREFIX = "DAYS:"

    /** ISO days 1=Mon..7=Sun parsed from "DAYS:1,2,3". Empty if not a days interval. */
    fun parseDays(repeatInterval: String): Set<Int> =
        if (!repeatInterval.startsWith(REPEAT_DAYS_PREFIX)) emptySet()
        else repeatInterval.removePrefix(REPEAT_DAYS_PREFIX).split(',')
            .mapNotNull { it.trim().toIntOrNull() }.filter { it in 1..7 }.toSet()

    fun daysInterval(days: Set<Int>): String = REPEAT_DAYS_PREFIX + days.sorted().joinToString(",")

    private fun isoDay(cal: Calendar): Int = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

    fun getNextReminderTime(currentTimeMillis: Long, repeatInterval: String): Long {
        if (repeatInterval == REPEAT_NONE || repeatInterval.isBlank()) return currentTimeMillis
        val days = parseDays(repeatInterval)
        val cal = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val now = System.currentTimeMillis()
        do {
            if (days.isNotEmpty()) cal.add(Calendar.DAY_OF_YEAR, 1)
            else when (repeatInterval) {
                REPEAT_DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                REPEAT_WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                REPEAT_MONTHLY -> cal.add(Calendar.MONTH, 1)
                REPEAT_YEARLY -> cal.add(Calendar.YEAR, 1)
                else -> return currentTimeMillis
            }
        } while (cal.timeInMillis <= now || (days.isNotEmpty() && isoDay(cal) !in days))
        return cal.timeInMillis
    }

    /** Shift first trigger forward to a selected day if [timeMillis] falls on an unselected day. */
    fun alignToRepeatDays(timeMillis: Long, repeatInterval: String): Long {
        val days = parseDays(repeatInterval)
        if (days.isEmpty()) return timeMillis
        val cal = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
        return if (isoDay(cal) in days) timeMillis else getNextReminderTime(timeMillis, repeatInterval)
    }

    fun getDaysLabel(days: Set<Int>, strings: AppStrings): String {
        val s = days.sorted()
        val n = strings.dayShortNames
        return when {
            s.size == 7 -> strings.repeatDaily
            s == listOf(6, 7) -> strings.weekend
            s.size > 2 && s.last() - s.first() == s.size - 1 -> "${n[s.first() - 1]}–${n[s.last() - 1]}"
            else -> s.joinToString(", ") { n[it - 1] }
        }
    }

    fun getRepeatLabel(repeatInterval: String, strings: AppStrings): String {
        return when (repeatInterval) {
            REPEAT_DAILY -> strings.repeatDaily
            REPEAT_WEEKLY -> strings.repeatWeekly
            REPEAT_MONTHLY -> strings.repeatMonthly
            REPEAT_YEARLY -> strings.repeatYearly
            else -> parseDays(repeatInterval).takeIf { it.isNotEmpty() }
                ?.let { getDaysLabel(it, strings) } ?: strings.repeatNone
        }
    }

    fun formatRemainingTime(
        targetHour: Int,
        targetMinute: Int,
        baseDateMillis: Long?,
        isAlarm: Boolean,
        strings: AppStrings
    ): String {
        val nowCal = Calendar.getInstance()
        val nowMillis = nowCal.timeInMillis

        val targetCal = Calendar.getInstance().apply {
            (baseDateMillis ?: nowMillis).let { timeInMillis = it }
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val isToday = (baseDateMillis == null) || (
            targetCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            targetCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
        )
        if (targetCal.timeInMillis <= nowMillis && isToday) {
            targetCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val diffMillis = targetCal.timeInMillis - nowMillis
        if (diffMillis <= 0L) {
            val prefix = if (isAlarm) strings.alarmIn else strings.reminderIn
            return prefix.format(strings.lessThanOneMinute)
        }

        val totalMinutes = (diffMillis + 59_999L) / 60_000L
        if (totalMinutes <= 0L) {
            val prefix = if (isAlarm) strings.alarmIn else strings.reminderIn
            return prefix.format(strings.lessThanOneMinute)
        }

        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60

        val durationString = buildString {
            if (days > 0) {
                append("$days ${strings.timeDays} ")
            }
            if (hours > 0) {
                append("$hours ${strings.timeHours} ")
            }
            if (minutes > 0 || (days == 0L && hours == 0L)) {
                append("$minutes ${strings.timeMinutes}")
            }
        }.trim()

        val prefix = if (isAlarm) strings.alarmIn else strings.reminderIn
        return prefix.format(durationString)
    }
}
