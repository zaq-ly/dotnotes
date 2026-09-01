package com.dotnotes.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val reminderTime: Long? = null,
    val priority: Int = 0,
    val isAlarmDismissed: Boolean = false,
    val snoozeDurationMin: Int = 5,
    val repeatInterval: String = "NONE",
    val colorTheme: String = "DEFAULT",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    companion object {
        fun getPreviewText(content: String): String {
            if (content.isBlank()) return ""
            val cleanHtml = content
                .replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n")
                .replace("</p>", "\n")
                .replace("</li>", "\n")
                .replace("</div>", "\n")
            val rawText = cleanHtml.replace(Regex("<[^>]*>"), "")
            return rawText.lines().firstOrNull { it.trim().isNotEmpty() }?.trim().orEmpty()
        }
    }
}

val Note.previewText: String
    get() = Note.getPreviewText(content)

