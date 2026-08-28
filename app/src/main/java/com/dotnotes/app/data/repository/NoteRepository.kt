package com.dotnotes.app.data.repository

import com.dotnotes.app.data.local.NoteDao
import com.dotnotes.app.data.model.Note

class NoteRepository(private val dao: NoteDao) {
    fun getAllNotes() = dao.getAllNotes()
    suspend fun getNoteById(id: String) = dao.getNoteById(id)
    suspend fun getNotesWithActiveReminders() = dao.getNotesWithActiveReminders()
    suspend fun upsertNote(note: Note) = dao.upsertNote(note)
    suspend fun softDeleteNote(id: String) = dao.softDeleteNote(id)
    suspend fun softDeleteNotes(ids: Collection<String>) = dao.softDeleteNotes(ids)
    suspend fun togglePin(id: String, isPinned: Boolean) = dao.togglePin(id, isPinned)
    suspend fun dismissAlarm(id: String) = dao.dismissAlarm(id)
    suspend fun clearReminder(id: String) = dao.clearReminder(id)
    suspend fun clearReminders(ids: Collection<String>) = dao.clearReminders(ids)
    suspend fun cleanExpiredCompletedReminders(thresholdTime: Long = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L) = dao.cleanExpiredCompletedReminders(thresholdTime)
}
