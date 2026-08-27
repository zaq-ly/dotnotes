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

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOnLockScreen()

        val noteId = intent.getStringExtra("note_id") ?: ""
        val noteTitle = intent.getStringExtra("note_title") ?: "Alarm"

        setContent {
            DotNotesTheme {
                AlarmScreen(
                    noteTitle = noteTitle,
                    onDismiss = { dismissAlarm(noteId) },
                    onSnooze = { snoozeAlarm(noteId) }
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    private fun dismissAlarm(noteId: String) {
        AlarmService.stop(this)
        NotificationManagerCompat.from(this).cancel(noteId.hashCode())
        runBlocking {
            DotNotesApp.instance.repository.dismissAlarm(noteId)
        }
        finish()
    }

    private fun snoozeAlarm(noteId: String) {
        AlarmService.stop(this)
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
}
