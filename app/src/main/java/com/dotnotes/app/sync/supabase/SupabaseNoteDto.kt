package com.dotnotes.app.sync.supabase

import com.dotnotes.app.data.model.Note
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SupabaseNoteDto(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("is_pinned") val isPinned: Boolean? = null,
    @SerialName("reminder_time") val reminderTime: Long? = null,
    @SerialName("priority") val priority: Int? = null,
    @SerialName("is_alarm_dismissed") val isAlarmDismissed: Boolean? = null,
    @SerialName("snooze_duration_min") val snoozeDurationMin: Int? = null,
    @SerialName("repeat_interval") val repeatInterval: String? = null,
    @SerialName("color_theme") val colorTheme: String? = null,
    @SerialName("auto_archive") val autoArchive: Boolean? = null,
    @SerialName("is_archived") val isArchived: Boolean? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("is_deleted") val isDeleted: Boolean? = null
) {
    fun toNote(userId: String? = null): Note {
        val decryptedTitle = if (userId != null && title != null) NoteCrypto.decrypt(title, userId) else title ?: ""
        val decryptedContent = if (userId != null && content != null) NoteCrypto.decrypt(content, userId) else content ?: ""
        return Note(
            id = if (id.isNotBlank()) id else UUID.randomUUID().toString(),
            title = decryptedTitle,
            content = decryptedContent,
            isPinned = isPinned ?: false,
            reminderTime = reminderTime,
            priority = priority ?: 0,
            isAlarmDismissed = isAlarmDismissed ?: false,
            snoozeDurationMin = snoozeDurationMin ?: 5,
            repeatInterval = repeatInterval ?: "NONE",
            colorTheme = colorTheme ?: "DEFAULT",
            autoArchive = autoArchive ?: true,
            isArchived = isArchived ?: false,
            createdAt = createdAt ?: System.currentTimeMillis(),
            updatedAt = updatedAt ?: System.currentTimeMillis(),
            isDeleted = isDeleted ?: false
        )
    }

    companion object {
        fun fromNote(note: Note, userId: String? = null): SupabaseNoteDto {
            val encTitle = if (userId != null) NoteCrypto.encrypt(note.title, userId) else note.title
            val encContent = if (userId != null) NoteCrypto.encrypt(note.content, userId) else note.content
            return SupabaseNoteDto(
                id = note.id,
                userId = userId,
                title = encTitle,
                content = encContent,
                isPinned = note.isPinned,
                reminderTime = note.reminderTime,
                priority = note.priority,
                isAlarmDismissed = note.isAlarmDismissed,
                snoozeDurationMin = note.snoozeDurationMin,
                repeatInterval = note.repeatInterval,
                colorTheme = note.colorTheme,
                autoArchive = note.autoArchive,
                isArchived = note.isArchived,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                isDeleted = note.isDeleted
            )
        }
    }
}
