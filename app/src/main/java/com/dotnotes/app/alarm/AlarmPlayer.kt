package com.dotnotes.app.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object AlarmPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isPlaying = false

    @Synchronized
    fun play(context: Context) {
        if (isPlaying) return
        isPlaying = true

        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "dotnotes:AlarmWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes max
            }
        } catch (_: Exception) {}

        startSound(context)
        startVibrate(context)
    }

    private fun getAlarmUri(): android.net.Uri? {
        val customAlarmUri: String? = try {
            runBlocking {
                kotlinx.coroutines.withTimeoutOrNull(400L) {
                    com.dotnotes.app.DotNotesApp.instance.settingsDataStore.alarmSoundUri.first()
                }
            }
        } catch (_: Exception) {
            null
        }
        return if (!customAlarmUri.isNullOrBlank()) {
            android.net.Uri.parse(customAlarmUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
        }
    }

    private fun startSound(context: Context) {
        try {
            val alarmUri = getAlarmUri()
            if (alarmUri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context.applicationContext, alarmUri)
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                    isLooping = true
                    prepare()
                    start()
                }
                return
            }
        } catch (_: Exception) {}

        // Fallback to Ringtone
        try {
            val ringtoneUri = getAlarmUri()
            if (ringtoneUri != null) {
                ringtone = RingtoneManager.getRingtone(context.applicationContext, ringtoneUri)?.apply {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        isLooping = true
                    }
                    play()
                }
            }
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun startVibrate(context: Context) {
        try {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            val pattern = longArrayOf(0, 800, 400, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) {}
    }

    @Synchronized
    fun stop() {
        isPlaying = false

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            ringtone?.stop()
        } catch (_: Exception) {}
        ringtone = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        vibrator = null

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }
}
