package com.dotnotes.app.ui.screens.editor

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotnotes.app.alarm.AlarmScheduler
import com.dotnotes.app.data.model.Note
import com.dotnotes.app.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class EditorState(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val hasReminder: Boolean = false,
    val reminderTime: Long? = null,
    val priority: Int = 0,
    val snoozeDurationMin: Int = 5,
    val repeatInterval: String = com.dotnotes.app.alarm.ReminderHelper.REPEAT_NONE,
    val createdAt: Long = 0L,
    val isLoading: Boolean = true
)

class NoteEditorViewModel(
    private val repository: NoteRepository,
    private val noteId: String?,
    private val app: Application
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state = _state.asStateFlow()

    init {
        if (noteId != null) {
            viewModelScope.launch {
                val note = repository.getNoteById(noteId)
                if (note != null) {
                    _state.value = EditorState(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        isPinned = note.isPinned,
                        hasReminder = note.reminderTime != null,
                        reminderTime = note.reminderTime,
                        priority = note.priority,
                        snoozeDurationMin = note.snoozeDurationMin,
                        repeatInterval = note.repeatInterval,
                        createdAt = note.createdAt,
                        isLoading = false
                    )
                } else {
                    _state.value = EditorState(isLoading = false)
                }
            }
        } else {
            _state.value = EditorState(isLoading = false)
        }
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _state.value = _state.value.copy(content = content)
    }

    fun setReminder(enabled: Boolean) {
        _state.value = _state.value.copy(
            hasReminder = enabled,
            reminderTime = if (enabled) _state.value.reminderTime else null,
            priority = if (enabled) maxOf(1, _state.value.priority) else 0,
            repeatInterval = if (enabled) _state.value.repeatInterval else com.dotnotes.app.alarm.ReminderHelper.REPEAT_NONE
        )
    }

    fun setReminderTime(timeMillis: Long) {
        _state.value = _state.value.copy(reminderTime = timeMillis)
    }

    fun setPriority(priority: Int) {
        _state.value = _state.value.copy(priority = priority)
    }

    fun setRepeatInterval(interval: String) {
        _state.value = _state.value.copy(repeatInterval = interval)
    }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val now = System.currentTimeMillis()
            val isNew = s.id.isEmpty()

            val note = Note(
                id = if (isNew) UUID.randomUUID().toString() else s.id,
                title = s.title,
                content = s.content,
                isPinned = s.isPinned,
                reminderTime = if (s.hasReminder) s.reminderTime else null,
                priority = if (s.hasReminder) s.priority else 0,
                snoozeDurationMin = s.snoozeDurationMin,
                repeatInterval = if (s.hasReminder) s.repeatInterval else com.dotnotes.app.alarm.ReminderHelper.REPEAT_NONE,
                createdAt = if (isNew) now else s.createdAt,
                updatedAt = now
            )
            repository.upsertNote(note)

            val scheduler = AlarmScheduler(app)
            if (note.reminderTime != null && note.reminderTime > now) {
                scheduler.schedule(note)
            } else {
                scheduler.cancel(note.id)
            }
        }
    }

    class Factory(
        private val repository: NoteRepository,
        private val noteId: String?,
        private val app: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NoteEditorViewModel(repository, noteId, app) as T
    }
}
