package com.dotnotes.app.sync

import android.content.Context
import com.dotnotes.app.data.local.NoteDao
import com.dotnotes.app.data.model.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class DriveSyncManager(private val noteDao: NoteDao) {
    private val gson = Gson()
    private val fileName = "dotnotes_backup.json"

    suspend fun sync(context: Context, email: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val token = GoogleAuthHelper.getAccessToken(context, email) ?: return@withContext false
                
                // 1. Get remote file info
                val fileId = getFileId(token)
                
                var remoteNotes = emptyList<Note>()
                if (fileId != null) {
                    remoteNotes = downloadNotes(token, fileId)
                }

                val localNotes = noteDao.getAllNotesList()

                // Merge
                val mergedNotes = mergeNotes(localNotes, remoteNotes)
                
                // Save back to DB
                noteDao.upsertNotes(mergedNotes)

                // Upload
                val mergedJson = gson.toJson(mergedNotes)
                if (fileId != null) {
                    updateFile(token, fileId, mergedJson)
                } else {
                    createFile(token, mergedJson)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun mergeNotes(local: List<Note>, remote: List<Note>): List<Note> {
        val remoteMap = remote.associateBy { it.id }
        val mergedMap = mutableMapOf<String, Note>()

        for (localNote in local) {
            val remoteNote = remoteMap[localNote.id]
            if (remoteNote != null) {
                if (localNote.updatedAt >= remoteNote.updatedAt) {
                    mergedMap[localNote.id] = localNote
                } else {
                    mergedMap[localNote.id] = remoteNote
                }
            } else {
                mergedMap[localNote.id] = localNote
            }
        }

        for (remoteNote in remote) {
            if (!mergedMap.containsKey(remoteNote.id)) {
                mergedMap[remoteNote.id] = remoteNote
            }
        }

        return mergedMap.values.toList()
    }

    private fun getFileId(token: String): String? {
        val url = URL("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='$fileName'")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val files = json.optJSONArray("files")
            if (files != null && files.length() > 0) {
                return files.getJSONObject(0).getString("id")
            }
        }
        return null
    }

    private fun downloadNotes(token: String, fileId: String): List<Note> {
        val url = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        
        if (conn.responseCode == 200) {
            InputStreamReader(conn.inputStream).use { reader ->
                val type = object : TypeToken<List<Note>>() {}.type
                return gson.fromJson(reader, type) ?: emptyList()
            }
        }
        return emptyList()
    }

    private fun createFile(token: String, content: String) {
        val boundary = "==boundary=="
        val url = URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        conn.doOutput = true

        val metadata = JSONObject()
        metadata.put("name", fileName)
        metadata.put("parents", org.json.JSONArray(listOf("appDataFolder")))

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write("--$boundary\r\n")
            writer.write("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            writer.write(metadata.toString())
            writer.write("\r\n")
            writer.write("--$boundary\r\n")
            writer.write("Content-Type: application/json\r\n\r\n")
            writer.write(content)
            writer.write("\r\n")
            writer.write("--$boundary--\r\n")
        }
        
        conn.responseCode
    }

    private fun updateFile(token: String, fileId: String, content: String) {
        val url = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(content)
        }
        
        conn.responseCode
    }
}
