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

        val noteTitle = intent.getStringExtra("note_title") ?: "Reminder"
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
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("note_id", noteId)
                putExtra("note_title", noteTitle)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            val notification = NotificationCompat.Builder(context, DotNotesApp.CHANNEL_REMINDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(".notes Reminder")
                .setContentText(noteTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(openPending)
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
    }
}
