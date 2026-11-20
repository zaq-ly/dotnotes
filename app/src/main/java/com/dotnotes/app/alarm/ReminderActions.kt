package com.dotnotes.app.alarm

import android.content.Context
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.data.model.Note

/** Shared Dismiss / Snooze logic for the alarm screen and notification buttons. */
object ReminderActions {
    fun isRecurring(note: Note): Boolean =
        note.repeatInterval.isNotBlank() && note.repeatInterval != ReminderHelper.REPEAT_NONE

    /** Moves a recurring reminder to its next occurrence and schedules it. Returns that time. */
    suspend fun advance(context: Context, note: Note): Long {
        val now = System.currentTimeMillis()
        val next = ReminderHelper.getNextReminderTime(note.reminderTime ?: now, note.repeatInterval)
        val updated = note.copy(reminderTime = next, isAlarmDismissed = false, updatedAt = now)
        DotNotesApp.instance.repository.upsertNote(updated)
        AlarmScheduler(context).schedule(updated)
        return next
    }

    /**
     * "Tandai Selesai" on a reminder that is ringing / notified.
     * Recurring reminders were already advanced when they rang, so this only closes the current
     * occurrence (older data whose time is still in the past is advanced here as before).
     * Returns the next reminder time for recurring reminders, null otherwise.
     */
    suspend fun dismissRinging(context: Context, noteId: String): Long? {
        val scheduler = AlarmScheduler(context)
        scheduler.cancelSnooze(noteId)
        val note = DotNotesApp.instance.repository.getNoteById(noteId) ?: return null
        if (isRecurring(note)) {
            val current = note.reminderTime
            if (current != null && current > System.currentTimeMillis()) {
                scheduler.schedule(note)
                return current
            }
            return advance(context, note)
        }
        DotNotesApp.instance.repository.dismissAlarm(noteId)
        scheduler.cancel(noteId)
        return null
    }

    /**
     * "Tunda": one-time reminders move their reminder time; recurring reminders get a separate
     * snooze alarm so the next scheduled occurrence stays intact.
     */
    suspend fun snooze(context: Context, noteId: String) {
        val note = DotNotesApp.instance.repository.getNoteById(noteId) ?: return
        val now = System.currentTimeMillis()
        val until = now + note.snoozeDurationMin * 60 * 1000L
        val scheduler = AlarmScheduler(context)
        if (isRecurring(note)) {
            scheduler.scheduleSnooze(note.id, until)
        } else {
            val snoozed = note.copy(reminderTime = until, isAlarmDismissed = false, updatedAt = now)
            DotNotesApp.instance.repository.upsertNote(snoozed)
            scheduler.schedule(snoozed)
        }
    }
}
