package com.dotnotes.app.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.MainActivity
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val noteId = intent.getStringExtra("note_id") ?: return

        if (action == ACTION_DISMISS) {
            AlarmService.stop(context)
            NotificationManagerCompat.from(context).cancel(noteId.hashCode())
            runBlocking {
                DotNotesApp.instance.repository.dismissAlarm(noteId)
            }
            return
        }

        if (action == ACTION_SNOOZE) {
            AlarmService.stop(context)
            NotificationManagerCompat.from(context).cancel(noteId.hashCode())
            runBlocking {
                val note = DotNotesApp.instance.repository.getNoteById(noteId)
                if (note != null) {
                    val snoozeMs = note.snoozeDurationMin * 60 * 1000L
                    val snoozedNote = note.copy(
                        reminderTime = System.currentTimeMillis() + snoozeMs,
                        isAlarmDismissed = false
                    )
                    DotNotesApp.instance.repository.upsertNote(snoozedNote)
                    AlarmScheduler(context).schedule(snoozedNote)
                }
            }
            return
        }

        val noteTitle = intent.getStringExtra("note_title")?.ifBlank { "Untitled" } ?: "Untitled"
        val rawContent = intent.getStringExtra("note_content") ?: ""
        val noteContent = rawContent.replace(Regex("<[^>]*>"), "").trim()
        val priority = intent.getIntExtra("priority", 1)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("note_id", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, noteId.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (priority == 2) {
            val alarmActivityIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra("note_id", noteId)
                putExtra("note_title", noteTitle)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
                )
            }
            try {
                context.startActivity(alarmActivityIntent)
            } catch (_: Exception) {
            }

            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("note_id", noteId)
                putExtra("note_title", noteTitle)
                putExtra("note_content", noteContent)
            }
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                // Fallback if background FGS start is denied by strict OEM OS
                val notification = NotificationCompat.Builder(context, DotNotesApp.CHANNEL_ALARM)
                    .setSmallIcon(com.dotnotes.app.R.mipmap.ic_launcher)
                    .setContentTitle(noteTitle)
                    .setContentText(if (noteContent.isNotBlank()) noteContent else "Pengingat Alarm")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(if (noteContent.isNotBlank()) noteContent else noteTitle))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(openPending)
                    .setAutoCancel(true)
                    .build()
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
                    android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                    NotificationManagerCompat.from(context).notify(noteId.hashCode(), notification)
                }
            }
        } else {
            val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
                this.action = ACTION_DISMISS
                putExtra("note_id", noteId)
            }
            val dismissPending = PendingIntent.getBroadcast(
                context, noteId.hashCode() + 2, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, DotNotesApp.CHANNEL_REMINDER)
                .setSmallIcon(com.dotnotes.app.R.mipmap.ic_launcher)
                .setContentTitle(noteTitle)
                .setContentText(if (noteContent.isNotBlank()) noteContent else "Pengingat Catatan")
                .setStyle(NotificationCompat.BigTextStyle().bigText(if (noteContent.isNotBlank()) noteContent else noteTitle))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(openPending)
                .addAction(android.R.drawable.checkbox_on_background, "Tandai Selesai", dismissPending)
                .setAutoCancel(true)
                .build()

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
                android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                NotificationManagerCompat.from(context).notify(noteId.hashCode(), notification)
            }
        }
    }

    companion object {
        const val ACTION_DISMISS = "com.dotnotes.app.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.dotnotes.app.ACTION_SNOOZE"
    }
}
