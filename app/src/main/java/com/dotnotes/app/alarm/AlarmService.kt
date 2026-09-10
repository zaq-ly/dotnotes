package com.dotnotes.app.alarm

import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.data.model.Note

class AlarmService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noteId = intent?.getStringExtra("note_id") ?: ""
        val noteTitle = intent?.getStringExtra("note_title")?.ifBlank { "Untitled" } ?: "Untitled"
        val rawContent = intent?.getStringExtra("note_content") ?: ""
        val noteContent = Note.getPreviewText(rawContent)

        val notifId = Math.abs(noteId.hashCode()) + 1

        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("note_id", noteId)
            putExtra("note_title", noteTitle)
            putExtra("note_content", noteContent)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        val options = ActivityOptions.makeBasic()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val method = ActivityOptions::class.java.getMethod(
                    "setPendingIntentCreatorBackgroundActivityStartMode",
                    Int::class.javaPrimitiveType
                )
                method.invoke(options, 1 /* MODE_BACKGROUND_ACTIVITY_START_ALLOWED */)
            } catch (_: Throwable) {
                try {
                    options.setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                } catch (_: Throwable) {}
            }
        }
        val optionsBundle = options.toBundle()

        val fullScreenPending = PendingIntent.getActivity(
            this, notifId + 4, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            optionsBundle
        )

        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS
            putExtra("note_id", noteId)
        }
        val dismissPending = PendingIntent.getBroadcast(
            this, notifId + 2, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE
            putExtra("note_id", noteId)
        }
        val snoozePending = PendingIntent.getBroadcast(
            this, notifId + 3, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissAction = NotificationCompat.Action.Builder(
            com.dotnotes.app.R.drawable.ic_stat_notification,
            "Tandai Selesai",
            dismissPending
        ).build()

        val snoozeAction = NotificationCompat.Action.Builder(
            com.dotnotes.app.R.drawable.ic_stat_notification,
            "Tunda",
            snoozePending
        ).build()

        val notification = NotificationCompat.Builder(this, DotNotesApp.CHANNEL_ALARM)
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
            .setAutoCancel(false)
            .setOngoing(true)
            .setNumber(1)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()
        notification.flags = notification.flags or android.app.Notification.FLAG_ONGOING_EVENT or android.app.Notification.FLAG_NO_CLEAR

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    notifId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(notifId, notification)
            }
        } catch (_: Exception) {
        }

        AlarmPlayer.play(this)

        try {
            startActivity(alarmIntent, optionsBundle)
        } catch (_: Exception) {
            try {
                startActivity(alarmIntent)
            } catch (_: Exception) {}
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        AlarmPlayer.stop()
        super.onDestroy()
    }

    companion object {
        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, AlarmService::class.java))
            } catch (_: Exception) {}
        }
    }
}
