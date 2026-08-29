package com.dotnotes.app.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.MainActivity

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noteId = intent?.getStringExtra("note_id") ?: ""
        val noteTitle = intent?.getStringExtra("note_title")?.ifBlank { "Untitled" } ?: "Untitled"
        val rawContent = intent?.getStringExtra("note_content") ?: ""
        val noteContent = rawContent.replace(Regex("<[^>]*>"), "").trim()

        val notifId = Math.abs(noteId.hashCode()) + 1

        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
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
        val optionsBundle = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.app.ActivityOptions.makeBasic().apply {
                setPendingIntentBackgroundActivityStartMode(
                    android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }.toBundle()
        } else {
            null
        }

        val fullScreenPending = if (optionsBundle != null) {
            PendingIntent.getActivity(
                this, notifId + 1, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                optionsBundle
            )
        } else {
            PendingIntent.getActivity(
                this, notifId + 1, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

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

        val notification = NotificationCompat.Builder(this, DotNotesApp.CHANNEL_ALARM)
            .setSmallIcon(com.dotnotes.app.R.drawable.ic_stat_notification)
            .setContentTitle(noteTitle)
            .setContentText(if (noteContent.isNotBlank()) noteContent else "Alarm Catatan")
            .setStyle(NotificationCompat.BigTextStyle().bigText(if (noteContent.isNotBlank()) noteContent else noteTitle))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPending, true)
            .setContentIntent(fullScreenPending)
            .addAction(android.R.drawable.checkbox_on_background, "Tandai Selesai", dismissPending)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Tunda", snoozePending)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        try {
            startForeground(notifId, notification)
        } catch (_: Exception) {
        }

        startAlarmSound()
        startVibration()

        try {
            startActivity(alarmIntent)
        } catch (_: Exception) {
        }

        return START_NOT_STICKY
    }

    private fun startAlarmSound() {
        if (mediaPlayer != null) return
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            if (alarmUri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(this@AlarmService, alarmUri)
                    isLooping = true
                    prepare()
                    start()
                }
            }
        } catch (_: Exception) {
            try {
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                ringtone?.play()
            } catch (_: Exception) {
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibration() {
        if (vibrator != null) return
        try {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            val pattern = longArrayOf(0, 800, 400, 800)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
        super.onDestroy()
    }

    companion object {
        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmService::class.java))
        }
    }
}
