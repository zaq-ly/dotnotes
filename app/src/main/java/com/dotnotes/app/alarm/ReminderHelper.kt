package com.dotnotes.app.alarm

import com.dotnotes.app.ui.i18n.AppStrings
import java.util.Calendar

object ReminderHelper {
    const val REPEAT_NONE = "NONE"
    const val REPEAT_DAILY = "DAILY"
    const val REPEAT_WEEKLY = "WEEKLY"
    const val REPEAT_MONTHLY = "MONTHLY"
    const val REPEAT_YEARLY = "YEARLY"

    fun getNextReminderTime(currentTimeMillis: Long, repeatInterval: String): Long {
        if (repeatInterval == REPEAT_NONE || repeatInterval.isBlank()) return currentTimeMillis
        val cal = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val now = System.currentTimeMillis()
        do {
            when (repeatInterval) {
                REPEAT_DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                REPEAT_WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                REPEAT_MONTHLY -> cal.add(Calendar.MONTH, 1)
                REPEAT_YEARLY -> cal.add(Calendar.YEAR, 1)
                else -> return currentTimeMillis
            }
        } while (cal.timeInMillis <= now)
        return cal.timeInMillis
    }

    fun getRepeatLabel(repeatInterval: String, strings: AppStrings): String {
        return when (repeatInterval) {
            REPEAT_DAILY -> strings.repeatDaily
            REPEAT_WEEKLY -> strings.repeatWeekly
            REPEAT_MONTHLY -> strings.repeatMonthly
            REPEAT_YEARLY -> strings.repeatYearly
            else -> strings.repeatNone
        }
    }
}
