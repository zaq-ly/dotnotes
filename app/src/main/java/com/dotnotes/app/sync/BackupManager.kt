package com.dotnotes.app.sync

import android.content.Context
import android.net.Uri
import com.dotnotes.app.data.local.NoteDao
import com.dotnotes.app.data.model.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(private val noteDao: NoteDao) {
    private val gson = Gson()

    suspend fun exportNotes(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val notes = noteDao.getAllNotesList()
                val json = gson.toJson(notes)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(json)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importNotes(context: Context, uri: Uri): Int {
        return withContext(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        reader.readText()
                    }
                } ?: return@withContext -1

                val type = object : TypeToken<List<Note>>() {}.type
                val importedNotes: List<Note> = gson.fromJson(json, type) ?: return@withContext -1

                val localNotes = noteDao.getAllNotesList()
                val remoteMap = importedNotes.associateBy { it.id }
                val mergedMap = mutableMapOf<String, Note>()

                for (localNote in localNotes) {
                    val remoteNote = remoteMap[localNote.id]
                    if (remoteNote == null) {
                        mergedMap[localNote.id] = localNote
                    } else {
                        if (localNote.updatedAt >= remoteNote.updatedAt) {
                            mergedMap[localNote.id] = localNote
                        } else {
                            mergedMap[localNote.id] = remoteNote
                        }
                    }
                }

                for (remoteNote in importedNotes) {
                    if (!mergedMap.containsKey(remoteNote.id)) {
                        mergedMap[remoteNote.id] = remoteNote
                    }
                }

                val mergedList = mergedMap.values.toList()
                noteDao.upsertNotes(mergedList)
                importedNotes.size
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }
    }
}
