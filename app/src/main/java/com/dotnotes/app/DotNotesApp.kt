package com.dotnotes.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.dotnotes.app.data.local.NoteDatabase
import com.dotnotes.app.data.preferences.SettingsDataStore
import com.dotnotes.app.data.repository.NoteRepository

class DotNotesApp : Application() {
    lateinit var database: NoteDatabase
    lateinit var repository: NoteRepository
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            this, NoteDatabase::class.java, "dotnotes.db"
        )
            .addMigrations(NoteDatabase.MIGRATION_1_2, NoteDatabase.MIGRATION_2_3)
            .build()

        repository = NoteRepository(database.noteDao())
        settingsDataStore = SettingsDataStore(this)

        createNotificationChannels()
        scheduleBackgroundUpdateCheck()
    }

    private fun scheduleBackgroundUpdateCheck() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val updateWorkRequest = androidx.work.PeriodicWorkRequestBuilder<com.dotnotes.app.update.UpdateCheckWorker>(
                12, java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                com.dotnotes.app.update.UpdateCheckWorker.WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                updateWorkRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannels() {
        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        val notifAudioAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val notifChannel = NotificationChannel(
            CHANNEL_REMINDER, "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Note reminders"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            setSound(soundUri, notifAudioAttributes)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
        }

        val alarmSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            ?: soundUri
        val alarmAudioAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM, "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Urgent note alarms"
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 800, 400, 800)
            setSound(alarmSoundUri, alarmAudioAttributes)
            setShowBadge(true)
        }

        val updateChannel = NotificationChannel(
            CHANNEL_UPDATE, "Pembaruan Aplikasi",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi rilis versi terbaru .notes"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
        }

        val mgr = getSystemService(NotificationManager::class.java)
        mgr?.createNotificationChannel(notifChannel)
        mgr?.createNotificationChannel(alarmChannel)
        mgr?.createNotificationChannel(updateChannel)
    }

    fun updateReminderChannelSound(soundUriString: String) {
        val soundUri = if (soundUriString.isNotBlank()) {
            android.net.Uri.parse(soundUriString)
        } else {
            android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        }
        val notifAudioAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val notifChannel = NotificationChannel(
            CHANNEL_REMINDER, "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Note reminders"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            setSound(soundUri, notifAudioAttributes)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
        }
        val mgr = getSystemService(NotificationManager::class.java)
        mgr?.createNotificationChannel(notifChannel)
    }

    companion object {
        lateinit var instance: DotNotesApp
        const val CHANNEL_REMINDER = "reminder_channel_v3"
        const val CHANNEL_ALARM = "alarm_channel_v3"
        const val CHANNEL_UPDATE = "update_channel_v1"
    }
}
