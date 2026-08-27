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
        ).build()

        repository = NoteRepository(database.noteDao())
        settingsDataStore = SettingsDataStore(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notifChannel = NotificationChannel(
            CHANNEL_REMINDER, "Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Note reminders"
        }

        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM, "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Urgent note alarms"
            setBypassDnd(true)
        }

        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(notifChannel)
        mgr.createNotificationChannel(alarmChannel)
    }

    companion object {
        lateinit var instance: DotNotesApp
        const val CHANNEL_REMINDER = "reminder_channel"
        const val CHANNEL_ALARM = "alarm_channel"
    }
}
