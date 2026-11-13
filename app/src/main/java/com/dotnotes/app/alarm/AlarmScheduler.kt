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

    fun schedule(note: Note) {
        val time = note.reminderTime ?: return
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

    fun cancel(noteId: String) {
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

        // Dismiss floating notifications and stop alarm audio immediately
        try {
            AlarmPlayer.stop()
            AlarmService.stop(context)
            val notifManager = androidx.core.app.NotificationManagerCompat.from(context)
            notifManager.cancel(noteId.hashCode())
            notifManager.cancel(Math.abs(noteId.hashCode()) + 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
