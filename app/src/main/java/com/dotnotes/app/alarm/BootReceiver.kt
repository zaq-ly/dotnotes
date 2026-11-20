package com.dotnotes.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dotnotes.app.DotNotesApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Restores alarms after a reboot or an app update.
 *
 * Missed reminders are never rung again here:
 * - Upcoming reminders are rescheduled.
 * - Missed recurring reminders are advanced to their next occurrence.
 * - Missed one-time reminders only get a silent notification after a reboot, and only when
 *   they were missed less than [MISSED_WINDOW_MS] ago. App updates never post anything.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bootActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )
        val isBoot = intent.action in bootActions
        if (!isBoot && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val scheduler = AlarmScheduler(appContext)
        val repo = DotNotesApp.instance.repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                repo.getNotesWithActiveReminders().forEach { note ->
                    try {
                        val time = note.reminderTime ?: return@forEach
                        val isRecurring = note.repeatInterval.isNotBlank() &&
                            note.repeatInterval != ReminderHelper.REPEAT_NONE
                        val missedRecently = isBoot && now - time in 0 until MISSED_WINDOW_MS

                        when {
                            time > now -> scheduler.schedule(note)
                            isRecurring -> {
                                val nextNote = note.copy(
                                    reminderTime = ReminderHelper.getNextReminderTime(time, note.repeatInterval),
                                    isAlarmDismissed = false,
                                    updatedAt = now
                                )
                                repo.upsertNote(nextNote)
                                scheduler.schedule(nextNote)
                                if (missedRecently) {
                                    AlarmReceiver.postStickyReminderNotification(appContext, note)
                                }
                            }
                            missedRecently -> AlarmReceiver.postStickyReminderNotification(appContext, note)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val MISSED_WINDOW_MS = 60 * 60 * 1000L
    }
}
