package com.dotnotes.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dotnotes.app.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note?

    @Query("SELECT * FROM notes WHERE reminderTime IS NOT NULL AND isDeleted = 0 AND isAlarmDismissed = 0")
    suspend fun getNotesWithActiveReminders(): List<Note>

    @Upsert
    suspend fun upsertNote(note: Note)

    @Upsert
    suspend fun upsertNotes(notes: List<Note>)

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesList(): List<Note>

    @Query("UPDATE notes SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDeleteNote(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isDeleted = 1, updatedAt = :now WHERE id IN (:ids)")
    suspend fun softDeleteNotes(ids: Collection<String>, now: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :now WHERE id = :id")
    suspend fun togglePin(id: String, isPinned: Boolean, now: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isAlarmDismissed = 1, updatedAt = :now WHERE id = :id")
    suspend fun dismissAlarm(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET reminderTime = NULL, priority = 0, isAlarmDismissed = 0, updatedAt = :now WHERE id = :id")
    suspend fun clearReminder(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET reminderTime = NULL, priority = 0, isAlarmDismissed = 0, updatedAt = :now WHERE id IN (:ids)")
    suspend fun clearReminders(ids: Collection<String>, now: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET reminderTime = NULL, priority = 0, isAlarmDismissed = 0, updatedAt = :now WHERE isAlarmDismissed = 1 AND updatedAt <= :thresholdTime")
    suspend fun cleanExpiredCompletedReminders(thresholdTime: Long, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteNotesPermanently(ids: Collection<String>)

    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()
}
