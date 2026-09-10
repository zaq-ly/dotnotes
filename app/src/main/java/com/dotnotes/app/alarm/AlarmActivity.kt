package com.dotnotes.app.alarm

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.NotificationManagerCompat
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.ui.screens.alarm.AlarmScreen
import com.dotnotes.app.ui.theme.DotNotesTheme
import kotlinx.coroutines.runBlocking

import androidx.compose.runtime.CompositionLocalProvider
import com.dotnotes.app.ui.i18n.EnglishStrings
import com.dotnotes.app.ui.i18n.IndonesianStrings
import com.dotnotes.app.ui.i18n.LocalStrings
import kotlinx.coroutines.flow.first

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        showOnLockScreen()
        super.onCreate(savedInstanceState)

        val noteId = intent.getStringExtra("note_id") ?: ""
        val noteTitle = intent.getStringExtra("note_title") ?: "Alarm"
        val noteContent = intent.getStringExtra("note_content") ?: ""
        val autoArchive = intent.getBooleanExtra("auto_archive", true)
        val repeatInterval = intent.getStringExtra("repeat_interval") ?: ""

        val note = runBlocking {
            DotNotesApp.instance.repository.getNoteById(noteId)
        }
        val effectiveRepeat = note?.repeatInterval ?: repeatInterval
        val hasRepeat = effectiveRepeat.isNotBlank() && effectiveRepeat != ReminderHelper.REPEAT_NONE

        val language = runBlocking {
            DotNotesApp.instance.settingsDataStore.language.first()
        }
        val strings = if (language == "id") IndonesianStrings else EnglishStrings

        setContent {
            CompositionLocalProvider(LocalStrings provides strings) {
                DotNotesTheme {
                    AlarmScreen(
                        noteTitle = noteTitle,
                        noteContent = noteContent,
                        autoArchive = autoArchive,
                        hasRepeat = hasRepeat,
                        onDismiss = { dismissAlarm(noteId) },
                        onSnooze = { snoozeAlarm(noteId) },
                        onStopRecurring = { stopRecurringAlarm(noteId) }
                    )
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showOnLockScreen()
    }

    override fun onResume() {
        super.onResume()
        showOnLockScreen()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showOnLockScreen()
    }

    @Suppress("DEPRECATION")
    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    private fun dismissAlarm(noteId: String) {
        AlarmPlayer.stop()
        AlarmService.stop(this)
        val notifId = Math.abs(noteId.hashCode()) + 1
        NotificationManagerCompat.from(this).cancel(notifId)
        NotificationManagerCompat.from(this).cancel(noteId.hashCode())
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
                AlarmScheduler(this@AlarmActivity).schedule(updatedNote)
            } else {
                DotNotesApp.instance.repository.dismissAlarm(noteId)
                AlarmScheduler(this@AlarmActivity).cancel(noteId)
            }
        }
        finish()
    }

    private fun stopRecurringAlarm(noteId: String) {
        AlarmPlayer.stop()
        AlarmService.stop(this)
        val notifId = Math.abs(noteId.hashCode()) + 1
        NotificationManagerCompat.from(this).cancel(notifId)
        NotificationManagerCompat.from(this).cancel(noteId.hashCode())
        runBlocking {
            DotNotesApp.instance.repository.stopRecurringAndDismissAlarm(noteId)
            AlarmScheduler(this@AlarmActivity).cancel(noteId)
        }
        finish()
    }

    private fun snoozeAlarm(noteId: String) {
        AlarmPlayer.stop()
        AlarmService.stop(this)
        val notifId = Math.abs(noteId.hashCode()) + 1
        NotificationManagerCompat.from(this).cancel(notifId)
        NotificationManagerCompat.from(this).cancel(noteId.hashCode())
        runBlocking {
            val note = DotNotesApp.instance.repository.getNoteById(noteId)
            if (note != null) {
                val snoozeMs = note.snoozeDurationMin * 60 * 1000L
                val snoozedNote = note.copy(
                    reminderTime = System.currentTimeMillis() + snoozeMs,
                    isAlarmDismissed = false
                )
                DotNotesApp.instance.repository.upsertNote(snoozedNote)
                AlarmScheduler(this@AlarmActivity).schedule(snoozedNote)
            }
        }
        finish()
    }

    override fun onDestroy() {
        AlarmPlayer.stop()
        AlarmService.stop(this)
        super.onDestroy()
    }
}
