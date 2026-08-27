package com.dotnotes.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotnotes.app.data.preferences.SettingsDataStore
import com.dotnotes.app.data.local.NoteDao
import android.net.Uri
import com.dotnotes.app.sync.BackupManager
import com.dotnotes.app.update.ReleaseInfo
import com.dotnotes.app.update.UpdateManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataStore: SettingsDataStore,
    private val noteDao: NoteDao
) : ViewModel() {
    private val updateManager = UpdateManager()

    val themeMode = dataStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val snoozeDuration = dataStore.snoozeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val language = dataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate = _isCheckingUpdate.asStateFlow()

    private val _availableUpdate = MutableStateFlow<ReleaseInfo?>(null)
    val availableUpdate = _availableUpdate.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress = _downloadProgress.asStateFlow()

    fun setThemeMode(mode: String) {
        viewModelScope.launch { dataStore.setThemeMode(mode) }
    }

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch { dataStore.setSnoozeDuration(minutes) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { dataStore.setLanguage(lang) }
    }

    fun exportBackup(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val backupManager = BackupManager(noteDao)
            val success = backupManager.exportNotes(context, uri)
            onResult(success)
        }
    }

    fun importBackup(context: Context, uri: Uri, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val backupManager = BackupManager(noteDao)
            val count = backupManager.importNotes(context, uri)
            onResult(count)
        }
    }

    fun checkForUpdate(currentVersion: String, onNoUpdate: () -> Unit = {}) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val info = updateManager.checkForUpdate(currentVersion)
            _isCheckingUpdate.value = false
            _availableUpdate.value = info
            if (info == null) {
                onNoUpdate()
            }
        }
    }

    fun dismissUpdateDialog() {
        _availableUpdate.value = null
    }

    fun downloadAndInstall(context: Context, releaseInfo: ReleaseInfo) {
        viewModelScope.launch {
            _downloadProgress.value = 0f
            val file = updateManager.downloadApk(context, releaseInfo.apkDownloadUrl) { progress ->
                _downloadProgress.value = progress
            }
            _downloadProgress.value = null
            if (file != null) {
                _availableUpdate.value = null
                updateManager.installApk(context, file)
            }
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
