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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
