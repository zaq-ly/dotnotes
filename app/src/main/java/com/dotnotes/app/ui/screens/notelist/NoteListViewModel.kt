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
    }

    fun checkForUpdate(currentVersion: String = "1.6.7") {
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

    fun togglePin(note: Note) {
        viewModelScope.launch { repository.togglePin(note.id, !note.isPinned) }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NoteListViewModel(repository) as T
    }
}
