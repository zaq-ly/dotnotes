package com.dotnotes.app.sync.supabase

import com.dotnotes.app.data.local.NoteDao
import com.dotnotes.app.data.model.Note
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SyncResult {
    data class Success(val syncedCount: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
    object NotLoggedIn : SyncResult()
    object NotConfigured : SyncResult()
}

class SupabaseSyncManager(private val noteDao: NoteDao) {
    private val client = SupabaseClientProvider.client

    suspend fun syncNotes(): SyncResult = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClientProvider.isConfigured) {
                return@withContext SyncResult.NotConfigured
            }

            val currentUser = client.auth.currentUserOrNull() ?: return@withContext SyncResult.NotLoggedIn
            val userId = currentUser.id

            // 1. Fetch remote notes from Supabase
            val remoteNotesDto = client.postgrest["notes"]
                .select()
                .decodeList<SupabaseNoteDto>()

            val remoteNotes = remoteNotesDto.map { it.toNote() }

            // 2. Fetch local notes
            val localNotes = noteDao.getAllNotesList()
            val localMap = localNotes.associateBy { it.id }

            // Collect deleted notes from local or remote for permanent purge
            val deletedIds = (localNotes.filter { it.isDeleted }.map { it.id } +
                              remoteNotes.filter { it.isDeleted }.map { it.id }).toSet()

            if (deletedIds.isNotEmpty()) {
                try {
                    client.postgrest["notes"].delete {
                        filter {
                            isIn("id", deletedIds.toList())
                        }
                    }
                } catch (_: Exception) {
                }
                noteDao.deleteNotesPermanently(deletedIds)
            }

            val activeLocalNotes = localNotes.filter { !it.isDeleted && !deletedIds.contains(it.id) }
            val activeRemoteNotes = remoteNotes.filter { !it.isDeleted && !deletedIds.contains(it.id) }
            val activeRemoteMap = activeRemoteNotes.associateBy { it.id }

            val toUploadToRemote = mutableListOf<SupabaseNoteDto>()
            val toSaveToLocal = mutableListOf<Note>()

            // 3. Compare active local notes with remote notes
            for (localNote in activeLocalNotes) {
                val remoteNote = activeRemoteMap[localNote.id]
                if (remoteNote == null) {
                    toUploadToRemote.add(SupabaseNoteDto.fromNote(localNote, userId))
                } else {
                    if (localNote.updatedAt > remoteNote.updatedAt) {
                        toUploadToRemote.add(SupabaseNoteDto.fromNote(localNote, userId))
                    } else if (remoteNote.updatedAt > localNote.updatedAt) {
                        toSaveToLocal.add(remoteNote)
                    }
                }
            }

            // 4. Remote notes not in local -> save locally
            for (remoteNote in activeRemoteNotes) {
                if (!localMap.containsKey(remoteNote.id)) {
                    toSaveToLocal.add(remoteNote)
                }
            }

            // 5. Upsert active notes to Supabase
            if (toUploadToRemote.isNotEmpty()) {
                client.postgrest["notes"].upsert(toUploadToRemote)
            }

            // 6. Upsert active notes to local Room
            if (toSaveToLocal.isNotEmpty()) {
                noteDao.upsertNotes(toSaveToLocal)
            }

            SyncResult.Success(syncedCount = toUploadToRemote.size + toSaveToLocal.size + deletedIds.size)
        } catch (e: Exception) {
            e.printStackTrace()
            SyncResult.Error(e.localizedMessage ?: "Sync error")
        }
    }
}
