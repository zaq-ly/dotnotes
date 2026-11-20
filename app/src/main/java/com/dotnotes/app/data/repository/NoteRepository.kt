package com.dotnotes.app.data.repository

import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.alarm.AlarmScheduler
import com.dotnotes.app.data.local.NoteDao
import com.dotnotes.app.data.model.Note
import com.dotnotes.app.sync.supabase.SupabaseSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteRepository(private val dao: NoteDao) {
    private val syncManager = SupabaseSyncManager(dao)
    private val syncScope = CoroutineScope(Dispatchers.IO)

    fun syncCloud() {
        syncScope.launch {
            syncManager.syncNotes()
        }
    }

    /** Removes pending alarms + notifications so no orphan alarm can ring for these notes. */
    private fun cancelAlarms(ids: Collection<String>) {
        try {
            val scheduler = AlarmScheduler(DotNotesApp.instance)
            ids.forEach { scheduler.cancel(it, stopPlayback = false) }
            com.dotnotes.app.alarm.FiredReminderStore.remove(DotNotesApp.instance, ids)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllNotes() = dao.getAllNotes()
    suspend fun getNoteById(id: String) = dao.getNoteById(id)
    suspend fun getNotesWithActiveReminders() = dao.getNotesWithActiveReminders()

    suspend fun upsertNote(note: Note) {
        dao.upsertNote(note)
        syncCloud()
    }

    suspend fun softDeleteNote(id: String) {
        cancelAlarms(listOf(id))
        dao.softDeleteNote(id)
        syncCloud()
    }

    suspend fun softDeleteNotes(ids: Collection<String>) {
        cancelAlarms(ids)
        dao.softDeleteNotes(ids)
        syncCloud()
    }

    suspend fun togglePin(id: String, isPinned: Boolean) {
        dao.togglePin(id, isPinned)
        syncCloud()
    }

    suspend fun togglePinNotes(ids: Collection<String>, isPinned: Boolean) {
        dao.togglePinNotes(ids, isPinned)
        syncCloud()
    }

    suspend fun dismissAlarm(id: String) {
        dao.dismissAlarm(id)
        syncCloud()
    }

    suspend fun stopRecurringAndDismissAlarm(id: String) {
        dao.stopRecurringAndDismissAlarm(id)
        syncCloud()
    }

    suspend fun archiveNotes(ids: Collection<String>) {
        cancelAlarms(ids)
        dao.archiveNotes(ids)
        syncCloud()
    }

    suspend fun restoreNotes(ids: Collection<String>) {
        cancelAlarms(ids)
        dao.restoreNotes(ids)
        syncCloud()
    }

    suspend fun clearReminder(id: String) {
        cancelAlarms(listOf(id))
        dao.clearReminder(id)
        syncCloud()
    }

    suspend fun clearReminders(ids: Collection<String>) {
        cancelAlarms(ids)
        dao.clearReminders(ids)
        syncCloud()
    }

    suspend fun cleanExpiredCompletedReminders(thresholdTime: Long = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L) {
        dao.cleanExpiredCompletedReminders(thresholdTime)
        syncCloud()
    }

    suspend fun clearAllNotes() = dao.clearAllNotes()
}
