package com.dotnotes.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dotnotes.app.MainActivity
import com.dotnotes.app.data.model.Note

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedules the note's alarm. [triggerAt] defaults to the reminder time; it is set explicitly
     * only to ring a missed reminder shortly after boot.
     */
    fun schedule(note: Note, triggerAt: Long? = note.reminderTime) {
        val time = triggerAt ?: return
        if (time <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("note_id", note.id)
            putExtra("note_title", note.title)
            putExtra("note_content", note.content)
            putExtra("priority", note.priority)
            putExtra("auto_archive", note.autoArchive)
            putExtra("repeat_interval", note.repeatInterval)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pending = PendingIntent.getBroadcast(
            context, note.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("note_id", note.id)
        }
        val showPending = PendingIntent.getActivity(
            context, note.id.hashCode() + 10, showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(time, pending, showPending)
    }

    /** Separate snooze alarm for a recurring reminder; the main schedule is left untouched. */
    fun scheduleSnooze(noteId: String, until: Long) {
        SnoozeStore.set(context, noteId, until)
        val showIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("note_id", noteId)
        }
        val showPending = PendingIntent.getActivity(
            context, noteId.hashCode() + 10, showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarm(until, snoozePending(noteId), showPending)
    }

    fun cancelSnooze(noteId: String) {
        SnoozeStore.remove(context, noteId)
        try {
            alarmManager.cancel(snoozePending(noteId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun snoozePending(noteId: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE_FIRE
            putExtra("note_id", noteId)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        return PendingIntent.getBroadcast(
            context, noteId.hashCode() + 20, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setAlarm(time: Long, pending: PendingIntent, showPending: PendingIntent) {
        try {
            // Priority 1: setAlarmClock (highest priority exact alarm, ignores Doze mode)
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(time, showPending), pending
            )
        } catch (e: Exception) {
            try {
                if (canScheduleExact()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
                }
            } catch (e2: Exception) {
                try {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
                } catch (e3: Exception) {
                    e3.printStackTrace()
                }
            }
        }
    }

    fun triggerNow(note: Note) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("note_id", note.id)
            putExtra("note_title", note.title)
            putExtra("note_content", note.content)
            putExtra("priority", note.priority)
            putExtra("auto_archive", note.autoArchive)
            putExtra("repeat_interval", note.repeatInterval)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        context.sendBroadcast(intent)
    }

    /**
     * Schedules the note if it still has an upcoming active reminder, otherwise removes any
     * pending alarm and its notifications. Used to keep AlarmManager in sync with the database.
     */
    fun reconcile(note: Note) {
        val time = note.reminderTime
        if (isActiveReminder(note) && time != null && time > System.currentTimeMillis()) {
            schedule(note)
        } else {
            cancel(note.id, stopPlayback = false)
        }
    }

    fun cancel(noteId: String, stopPlayback: Boolean = true) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, noteId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pending)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cancelSnooze(noteId)

        // Dismiss floating notifications and stop alarm audio immediately
        try {
            if (stopPlayback) {
                AlarmPlayer.stop()
                AlarmService.stop(context)
            }
            val notifManager = androidx.core.app.NotificationManagerCompat.from(context)
            notifManager.cancel(noteId.hashCode())
            notifManager.cancel(Math.abs(noteId.hashCode()) + 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun isActiveReminder(note: Note): Boolean =
            note.reminderTime != null && !note.isDeleted && !note.isArchived && !note.isAlarmDismissed
    }
}
