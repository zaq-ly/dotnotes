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
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("is_deleted") val isDeleted: Boolean? = null
) {
    fun toNote(): Note = Note(
        id = if (id.isNotBlank()) id else UUID.randomUUID().toString(),
        title = title ?: "",
        content = content ?: "",
        isPinned = isPinned ?: false,
        reminderTime = reminderTime,
        priority = priority ?: 0,
        isAlarmDismissed = isAlarmDismissed ?: false,
        snoozeDurationMin = snoozeDurationMin ?: 5,
        createdAt = createdAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis(),
        isDeleted = isDeleted ?: false
    )

    companion object {
        fun fromNote(note: Note, userId: String? = null): SupabaseNoteDto = SupabaseNoteDto(
            id = note.id,
            userId = userId,
            title = note.title,
            content = note.content,
            isPinned = note.isPinned,
            reminderTime = note.reminderTime,
            priority = note.priority,
            isAlarmDismissed = note.isAlarmDismissed,
            snoozeDurationMin = note.snoozeDurationMin,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            isDeleted = note.isDeleted
        )
    }
}
