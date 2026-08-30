package com.dotnotes.app.ui.screens.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotnotes.app.data.model.Note
import com.dotnotes.app.data.repository.NoteRepository
import com.dotnotes.app.update.UpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteListViewModel(
    private val repository: NoteRepository,
    private val updateManager: UpdateManager = UpdateManager()
) : ViewModel() {
    val notes = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hasUpdate = MutableStateFlow(false)
    val hasUpdate: StateFlow<Boolean> = _hasUpdate

    init {
        checkForUpdate()
        cleanExpiredReminders()
    }

    fun cleanExpiredReminders() {
        viewModelScope.launch {
            repository.cleanExpiredCompletedReminders()
        }
    }

    fun checkForUpdate(currentVersion: String = "1.13.4") {
        viewModelScope.launch {
            val release = updateManager.checkForUpdate(currentVersion)
            _hasUpdate.value = (release != null)
            if (release != null) {
                updateManager.showUpdateNotification(com.dotnotes.app.DotNotesApp.instance, release)
            }
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
            noteIds.forEach { scheduler.cancel(it) }
            repository.clearReminders(noteIds)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { repository.togglePin(note.id, !note.isPinned) }
    }

    fun dismissReminder(context: android.content.Context, noteId: String) {
        viewModelScope.launch {
            repository.dismissAlarm(noteId)
            com.dotnotes.app.alarm.AlarmScheduler(context).cancel(noteId)
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NoteListViewModel(repository) as T
    }
}
