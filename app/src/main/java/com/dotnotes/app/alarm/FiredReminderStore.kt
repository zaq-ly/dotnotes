package com.dotnotes.app.alarm

import android.content.Context
import com.dotnotes.app.data.model.Note

/**
 * Remembers which reminder occurrences have already rung on this device (noteId -> reminderTime).
 *
 * Used after a reboot / app update to tell apart a reminder that was missed while the phone was
 * off (must still ring) from one that already rang and was silenced by the user (must not ring).
 */
object FiredReminderStore {
    private const val PREFS = "fired_reminders"
    private const val KEY_INITIALIZED = "__initialized"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun markFired(context: Context, noteId: String, reminderTime: Long) {
        prefs(context).edit().putLong(noteId, reminderTime).apply()
    }

    fun wasFired(context: Context, noteId: String, reminderTime: Long): Boolean =
        prefs(context).getLong(noteId, Long.MIN_VALUE) == reminderTime

    fun remove(context: Context, noteIds: Collection<String>) {
        val editor = prefs(context).edit()
        noteIds.forEach { editor.remove(it) }
        editor.apply()
    }

    /**
     * First run after upgrading from a version without this store: every reminder that is already
     * in the past is treated as "already rang", so old silenced reminders never ring again.
     * Returns true if seeding happened on this call.
     */
    fun seedIfNeeded(context: Context, activeReminders: List<Note>, now: Long): Boolean {
        val p = prefs(context)
        if (p.getBoolean(KEY_INITIALIZED, false)) return false
        val editor = p.edit()
        activeReminders.forEach { note ->
            val time = note.reminderTime ?: return@forEach
            if (time <= now) editor.putLong(note.id, time)
        }
        editor.putBoolean(KEY_INITIALIZED, true).commit()
        return true
    }
}
