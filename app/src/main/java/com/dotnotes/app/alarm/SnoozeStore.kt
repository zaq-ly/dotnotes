package com.dotnotes.app.alarm

import android.content.Context

/**
 * Pending snoozes of recurring reminders (noteId -> snooze-until millis).
 *
 * A recurring reminder's main schedule already points at its next occurrence once it rings, so a
 * snooze is kept as a separate alarm that must survive reboots without touching that schedule.
 */
object SnoozeStore {
    private const val PREFS = "recurring_snoozes"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun set(context: Context, noteId: String, until: Long) {
        prefs(context).edit().putLong(noteId, until).apply()
    }

    fun remove(context: Context, noteId: String) {
        prefs(context).edit().remove(noteId).apply()
    }

    fun all(context: Context): Map<String, Long> =
        prefs(context).all.mapNotNull { (key, value) -> (value as? Long)?.let { key to it } }.toMap()
}
