package com.dotnotes.app.data.repository

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

    fun getAllNotes() = dao.getAllNotes()
    suspend fun getNoteById(id: String) = dao.getNoteById(id)
    suspend fun getNotesWithActiveReminders() = dao.getNotesWithActiveReminders()

    suspend fun upsertNote(note: Note) {
        dao.upsertNote(note)
        syncCloud()
    }

    suspend fun softDeleteNote(id: String) {
        dao.softDeleteNote(id)
        syncCloud()
    }

    suspend fun softDeleteNotes(ids: Collection<String>) {
        dao.softDeleteNotes(ids)
        syncCloud()
    }

    suspend fun togglePin(id: String, isPinned: Boolean) {
        dao.togglePin(id, isPinned)
        syncCloud()
    }

    suspend fun dismissAlarm(id: String) {
        dao.dismissAlarm(id)
        syncCloud()
    }

    suspend fun clearReminder(id: String) {
        dao.clearReminder(id)
        syncCloud()
    }

    suspend fun clearReminders(ids: Collection<String>) {
        dao.clearReminders(ids)
        syncCloud()
    }

    suspend fun cleanExpiredCompletedReminders(thresholdTime: Long = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L) {
        dao.cleanExpiredCompletedReminders(thresholdTime)
        syncCloud()
    }

    suspend fun clearAllNotes() = dao.clearAllNotes()
}
