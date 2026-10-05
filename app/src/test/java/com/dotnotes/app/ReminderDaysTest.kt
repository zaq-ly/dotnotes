package com.dotnotes.app

import com.dotnotes.app.alarm.ReminderHelper
import com.dotnotes.app.ui.i18n.IndonesianStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderDaysTest {
    private fun isoDay(ms: Long) = Calendar.getInstance().apply { timeInMillis = ms }
        .let { (it.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 }

    /** Future Saturday 08:30, safely after now. */
    private fun futureSaturday(): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 14)
        while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 30); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun parseAndFormat() {
        assertEquals(setOf(1, 2, 3, 4, 5), ReminderHelper.parseDays("DAYS:1,2,3,4,5"))
        assertEquals(emptySet<Int>(), ReminderHelper.parseDays("DAILY"))
        assertEquals(setOf(1, 7), ReminderHelper.parseDays("DAYS:7,x,1,9"))
        assertEquals("DAYS:1,3,5", ReminderHelper.daysInterval(setOf(5, 1, 3)))
    }

    @Test
    fun weekdaysSkipWeekend() {
        val sat = futureSaturday()
        val interval = ReminderHelper.daysInterval((1..5).toSet())
        val aligned = ReminderHelper.alignToRepeatDays(sat, interval)
        assertEquals(1, isoDay(aligned)) // Saturday shifts to Monday
        assertEquals(2 * 24 * 60 * 60 * 1000L, aligned - sat)
        val next = ReminderHelper.getNextReminderTime(aligned, interval)
        assertEquals(2, isoDay(next))
        // Friday -> next Monday
        val fri = aligned + 4 * 24 * 60 * 60 * 1000L
        assertEquals(1, isoDay(ReminderHelper.getNextReminderTime(fri, interval)))
        // selected day stays as-is
        assertEquals(sat, ReminderHelper.alignToRepeatDays(sat, ReminderHelper.daysInterval(setOf(6))))
        assertTrue(ReminderHelper.getNextReminderTime(sat, "DAYS:6") - sat == 7 * 24 * 60 * 60 * 1000L)
    }

    @Test
    fun labels() {
        val s = IndonesianStrings
        assertEquals("Sen–Jum", ReminderHelper.getRepeatLabel("DAYS:1,2,3,4,5", s))
        assertEquals("Sen–Sab", ReminderHelper.getRepeatLabel("DAYS:1,2,3,4,5,6", s))
        assertEquals("Akhir pekan", ReminderHelper.getRepeatLabel("DAYS:6,7", s))
        assertEquals("Setiap hari", ReminderHelper.getRepeatLabel("DAYS:1,2,3,4,5,6,7", s))
        assertEquals("Sen, Rab, Jum", ReminderHelper.getRepeatLabel("DAYS:1,3,5", s))
        assertEquals("Tidak berulang", ReminderHelper.getRepeatLabel("DAYS:", s))
    }
}
