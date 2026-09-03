package com.dotnotes.app.alarm

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.MainActivity
import com.dotnotes.app.data.model.Note
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val noteId = intent.getStringExtra("note_id") ?: return

        if (action == ACTION_DISMISS) {
            AlarmPlayer.stop()
            AlarmService.stop(context)
            NotificationManagerCompat.from(context).cancel(noteId.hashCode())
            NotificationManagerCompat.from(context).cancel(Math.abs(noteId.hashCode()) + 1)
            runBlocking {
                val note = DotNotesApp.instance.repository.getNoteById(noteId)
                if (note != null && note.repeatInterval != ReminderHelper.REPEAT_NONE && note.repeatInterval.isNotBlank()) {
                    val nextTime = ReminderHelper.getNextReminderTime(note.reminderTime ?: System.currentTimeMillis(), note.repeatInterval)
                    val updatedNote = note.copy(
                        reminderTime = nextTime,
                        isAlarmDismissed = false,
                        updatedAt = System.currentTimeMillis()
                    )
                    DotNotesApp.instance.repository.upsertNote(updatedNote)
                    AlarmScheduler(context).schedule(updatedNote)
                } else {
                    DotNotesApp.instance.repository.dismissAlarm(noteId)
                }
            }
            return
        }

        if (action == ACTION_SWIPE) {
            runBlocking {
                val note = DotNotesApp.instance.repository.getNoteById(noteId)
                if (note != null && !note.isAlarmDismissed && note.reminderTime != null) {
                    postStickyReminderNotification(context, note)
                }
            }
            return
        }

        if (action == ACTION_SNOOZE) {
            AlarmPlayer.stop()
            AlarmService.stop(context)
            NotificationManagerCompat.from(context).cancel(noteId.hashCode())
            NotificationManagerCompat.from(context).cancel(Math.abs(noteId.hashCode()) + 1)
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
        val noteContent = Note.getPreviewText(rawContent)
        val priority = intent.getIntExtra("priority", 1)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("note_id", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, noteId.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifId = Math.abs(noteId.hashCode()) + 1

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_DISMISS
            putExtra("note_id", noteId)
        }
        val dismissPending = PendingIntent.getBroadcast(
            context, notifId + 2, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissAction = NotificationCompat.Action.Builder(
            com.dotnotes.app.R.drawable.ic_stat_notification,
            "Tandai Selesai",
            dismissPending
        ).build()

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_SNOOZE
            putExtra("note_id", noteId)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context, notifId + 3, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeAction = NotificationCompat.Action.Builder(
            com.dotnotes.app.R.drawable.ic_stat_notification,
            "Tunda",
            snoozePending
        ).build()

        val swipeIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_SWIPE
            putExtra("note_id", noteId)
        }
        val swipePending = PendingIntent.getBroadcast(
            context, notifId + 5, swipeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (priority == 2) {
            AlarmPlayer.play(context)

            val alarmActivityIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra("note_id", noteId)
                putExtra("note_title", noteTitle)
                putExtra("note_content", noteContent)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
            }

            val fullScreenPending = PendingIntent.getActivity(
                context, notifId + 4, alarmActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, DotNotesApp.CHANNEL_ALARM)
                .setSmallIcon(com.dotnotes.app.R.drawable.ic_stat_notification)
                .setContentTitle(noteTitle)
                .setContentText(if (noteContent.isNotBlank()) noteContent else "Pengingat Alarm")
                .setStyle(NotificationCompat.BigTextStyle().bigText(if (noteContent.isNotBlank()) noteContent else noteTitle))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenPending, true)
                .setContentIntent(fullScreenPending)
                .addAction(dismissAction)
                .addAction(snoozeAction)
                .setColor(0xFFBE123C.toInt())
                .setDeleteIntent(swipePending)
                .setNumber(1)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .build()

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                NotificationManagerCompat.from(context).notify(notifId, notification)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val options = ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(
                            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        )
                    }.toBundle()
                    context.startActivity(alarmActivityIntent, options)
                } else {
                    context.startActivity(alarmActivityIntent)
                }
            } catch (_: Exception) {
            }
        } else {
            val notification = NotificationCompat.Builder(context, DotNotesApp.CHANNEL_REMINDER)
                .setSmallIcon(com.dotnotes.app.R.drawable.ic_stat_notification)
                .setContentTitle(noteTitle)
                .setContentText(if (noteContent.isNotBlank()) noteContent else "Pengingat Catatan")
                .setStyle(NotificationCompat.BigTextStyle().bigText(if (noteContent.isNotBlank()) noteContent else noteTitle))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(openPending)
                .addAction(dismissAction)
                .addAction(snoozeAction)
                .setColor(0xFF1D4ED8.toInt())
                .setDeleteIntent(swipePending)
                .setNumber(1)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .build()

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                NotificationManagerCompat.from(context).notify(notifId, notification)
            }
        }
    }

    private fun postStickyReminderNotification(context: Context, note: Note) {
        val noteId = note.id
        val notifId = Math.abs(noteId.hashCode()) + 1
        val noteTitle = note.title.ifBlank { "Untitled" }
        val noteContent = Note.getPreviewText(note.content)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("note_id", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, noteId.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_DISMISS
            putExtra("note_id", noteId)
        }
        val dismissPending = PendingIntent.getBroadcast(
            context, notifId + 2, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissAction = NotificationCompat.Action.Builder(
            com.dotnotes.app.R.drawable.ic_stat_notification,
            "Tandai Selesai",
            dismissPending
        ).build()

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_SNOOZE
            putExtra("note_id", noteId)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context, notifId + 3, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeAction = NotificationCompat.Action.Builder(
            com.dotnotes.app.R.drawable.ic_stat_notification,
            "Tunda",
            snoozePending
        ).build()

        val swipeIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_SWIPE
            putExtra("note_id", noteId)
        }
        val swipePending = PendingIntent.getBroadcast(
            context, notifId + 5, swipeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DotNotesApp.CHANNEL_REMINDER)
            .setSmallIcon(com.dotnotes.app.R.drawable.ic_stat_notification)
            .setContentTitle(noteTitle)
            .setContentText(if (noteContent.isNotBlank()) noteContent else "Pengingat Catatan")
            .setStyle(NotificationCompat.BigTextStyle().bigText(if (noteContent.isNotBlank()) noteContent else noteTitle))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openPending)
            .addAction(dismissAction)
            .addAction(snoozeAction)
            .setColor(0xFF1D4ED8.toInt())
            .setDeleteIntent(swipePending)
            .setSilent(true)
            .setNumber(1)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        }
    }

    companion object {
        const val ACTION_DISMISS = "com.dotnotes.app.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.dotnotes.app.ACTION_SNOOZE"
        const val ACTION_SWIPE = "com.dotnotes.app.ACTION_SWIPE"
    }
}
