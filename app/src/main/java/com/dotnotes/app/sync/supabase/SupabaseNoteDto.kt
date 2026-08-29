package com.dotnotes.app.sync.supabase

import com.dotnotes.app.data.model.Note
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseNoteDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("title") val title: String = "",
    @SerialName("content") val content: String = "",
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("reminder_time") val reminderTime: Long? = null,
    @SerialName("priority") val priority: Int = 0,
    @SerialName("is_alarm_dismissed") val isAlarmDismissed: Boolean = false,
    @SerialName("snooze_duration_min") val snoozeDurationMin: Int = 5,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
    @SerialName("is_deleted") val isDeleted: Boolean = false
) {
    fun toNote(): Note = Note(
        id = id,
        title = title,
        content = content,
        isPinned = isPinned,
        reminderTime = reminderTime,
        priority = priority,
        isAlarmDismissed = isAlarmDismissed,
        snoozeDurationMin = snoozeDurationMin,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
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
