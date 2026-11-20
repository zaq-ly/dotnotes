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
 * - Upcoming reminders are rescheduled as usual.
 * - Reminders missed while the phone was off (never rang, no matter how long ago) are rung
 *   a few seconds after boot through AlarmManager, so the full-screen alarm path is allowed.
 * - Reminders that already rang (and were silenced by the user) are never rung again.
 * - Pending snoozes of recurring reminders are restored.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )
        if (intent.action !in validActions) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val scheduler = AlarmScheduler(appContext)
        val repo = DotNotesApp.instance.repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val reminders = repo.getNotesWithActiveReminders()
                FiredReminderStore.seedIfNeeded(appContext, reminders, now)

                var missedIndex = 0
                reminders.sortedBy { it.reminderTime }.forEach { note ->
                    try {
                        val time = note.reminderTime ?: return@forEach
                        when {
                            time > now -> scheduler.schedule(note)
                            !FiredReminderStore.wasFired(appContext, note.id, time) -> {
                                // Missed while the phone was off: ring it shortly after boot,
                                // staggered so several missed reminders don't collide.
                                val triggerAt = now + MISSED_DELAY_MS + missedIndex * MISSED_STAGGER_MS
                                scheduler.schedule(note, triggerAt)
                                missedIndex++
                            }
                            // Already rang but was never moved on (older data): advance quietly.
                            ReminderActions.isRecurring(note) -> ReminderActions.advance(appContext, note)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Restore pending snoozes of recurring reminders.
                SnoozeStore.all(appContext).forEach { (noteId, until) ->
                    try {
                        val note = repo.getNoteById(noteId)
                        if (note != null && AlarmScheduler.isActiveReminder(note)) {
                            val minTime = now + MISSED_DELAY_MS + missedIndex * MISSED_STAGGER_MS
                            if (until < minTime) missedIndex++
                            scheduler.scheduleSnooze(noteId, maxOf(until, minTime))
                        } else {
                            SnoozeStore.remove(appContext, noteId)
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
        private const val MISSED_DELAY_MS = 5_000L
        private const val MISSED_STAGGER_MS = 3_000L
    }
}
