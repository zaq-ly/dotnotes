package com.dotnotes.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotnotes.app.data.preferences.SettingsDataStore
import com.dotnotes.app.data.local.NoteDao
import com.dotnotes.app.sync.DriveSyncManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataStore: SettingsDataStore,
    private val noteDao: NoteDao
) : ViewModel() {
    val themeMode = dataStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val snoozeDuration = dataStore.snoozeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val googleEmail = dataStore.googleEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    fun setThemeMode(mode: String) {
        viewModelScope.launch { dataStore.setThemeMode(mode) }
    }

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch { dataStore.setSnoozeDuration(minutes) }
    }

    fun setGoogleEmail(email: String?) {
        viewModelScope.launch { dataStore.setGoogleEmail(email) }
    }

    fun syncNotes(context: Context) {
        val email = googleEmail.value ?: return
        viewModelScope.launch {
            _isSyncing.value = true
            val syncManager = DriveSyncManager(noteDao)
            val success = syncManager.sync(context, email)
            _isSyncing.value = false
            // Optional: emit success/failure state to show toast
        }
    }

    class Factory(
        private val dataStore: SettingsDataStore,
        private val noteDao: NoteDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(dataStore, noteDao) as T
    }
}
