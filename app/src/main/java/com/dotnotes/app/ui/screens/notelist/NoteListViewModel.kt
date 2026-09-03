package com.dotnotes.app.ui.screens.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotnotes.app.BuildConfig
import com.dotnotes.app.data.model.Note
import com.dotnotes.app.data.repository.NoteRepository
import com.dotnotes.app.update.UpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class NoteListViewModel(
    private val repository: NoteRepository,
    private val updateManager: UpdateManager = UpdateManager()
) : ViewModel() {
    val notes = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hasUpdate = MutableStateFlow(false)
    val hasUpdate: StateFlow<Boolean> = _hasUpdate

    init {
        startPeriodicUpdateChecker()
        cleanExpiredReminders()
        repository.syncCloud()
    }

    private fun startPeriodicUpdateChecker() {
        viewModelScope.launch {
            while (isActive) {
                checkForUpdate(BuildConfig.VERSION_NAME)
                delay(15 * 60 * 1000L) // 15 menit loop real-time
            }
        }
    }

    fun cleanExpiredReminders() {
        viewModelScope.launch {
            repository.cleanExpiredCompletedReminders()
        }
    }

    fun checkForUpdate(currentVersion: String = BuildConfig.VERSION_NAME) {
        viewModelScope.launch {
            val release = updateManager.checkForUpdate(currentVersion)
            _hasUpdate.value = (release != null)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch { repository.softDeleteNote(noteId) }
    }

    fun deleteNotes(noteIds: Collection<String>) {
        viewModelScope.launch { repository.softDeleteNotes(noteIds) }
    }

    fun deleteHistoryReminders(context: android.content.Context, noteIds: Collection<String>) {
        viewModelScope.launch {
            val scheduler = com.dotnotes.app.alarm.AlarmScheduler(context)
            val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
            noteIds.forEach {
                scheduler.cancel(it)
                notificationManager.cancel(it.hashCode())
                notificationManager.cancel(Math.abs(it.hashCode()) + 1)
                notificationManager.cancel(Math.abs(it.hashCode()) + 10)
            }
            repository.clearReminders(noteIds)
            com.dotnotes.app.alarm.AlarmReceiver.updateMiuiBadgeCount(context)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { repository.togglePin(note.id, !note.isPinned) }
    }

    fun togglePinNotes(noteIds: Collection<String>, shouldPin: Boolean) {
        viewModelScope.launch { repository.togglePinNotes(noteIds, shouldPin) }
    }

    fun dismissReminder(context: android.content.Context, noteId: String) {
        viewModelScope.launch {
            val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
            notificationManager.cancel(noteId.hashCode())
            notificationManager.cancel(Math.abs(noteId.hashCode()) + 1)
            notificationManager.cancel(Math.abs(noteId.hashCode()) + 10)

            val note = repository.getNoteById(noteId)
            if (note != null && note.repeatInterval != com.dotnotes.app.alarm.ReminderHelper.REPEAT_NONE && note.repeatInterval.isNotBlank()) {
                val nextTime = com.dotnotes.app.alarm.ReminderHelper.getNextReminderTime(note.reminderTime ?: System.currentTimeMillis(), note.repeatInterval)
                val updatedNote = note.copy(
                    reminderTime = nextTime,
                    isAlarmDismissed = false,
                    updatedAt = System.currentTimeMillis()
                )
                repository.upsertNote(updatedNote)
                com.dotnotes.app.alarm.AlarmScheduler(context).schedule(updatedNote)
            } else {
                repository.dismissAlarm(noteId)
                com.dotnotes.app.alarm.AlarmScheduler(context).cancel(noteId)
            }
            com.dotnotes.app.alarm.AlarmReceiver.updateMiuiBadgeCount(context)
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NoteListViewModel(repository) as T
    }
}
