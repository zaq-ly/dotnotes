package com.dotnotes.app.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotnotes.app.data.local.NoteDao
import com.dotnotes.app.data.preferences.SettingsDataStore
import com.dotnotes.app.sync.BackupManager
import com.dotnotes.app.sync.supabase.AuthUserState
import com.dotnotes.app.sync.supabase.GoogleAuthManager
import com.dotnotes.app.sync.supabase.SupabaseClientProvider
import com.dotnotes.app.sync.supabase.SupabaseSyncManager
import com.dotnotes.app.sync.supabase.SyncResult
import com.dotnotes.app.update.ReleaseInfo
import com.dotnotes.app.update.UpdateManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataStore: SettingsDataStore,
    private val noteDao: NoteDao
) : ViewModel() {
    private val updateManager = UpdateManager()
    private val syncManager = SupabaseSyncManager(noteDao)

    val themeMode = dataStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val snoozeDuration = dataStore.snoozeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val language = dataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val lastSyncTime = dataStore.lastSyncTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val authUserState = SupabaseClientProvider.client.auth.sessionStatus.map {
        val user = SupabaseClientProvider.client.auth.currentUserOrNull()
        if (user != null) {
            val metadata = user.userMetadata
            val name = metadata?.get("full_name")?.toString()?.trim('\"')
                ?: metadata?.get("name")?.toString()?.trim('\"')
            val avatar = metadata?.get("avatar_url")?.toString()?.trim('\"')
                ?: metadata?.get("picture")?.toString()?.trim('\"')
            AuthUserState(
                isLoggedIn = true,
                email = user.email,
                displayName = name,
                avatarUrl = avatar
            )
        } else {
            AuthUserState(isLoggedIn = false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthUserState())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate = _isCheckingUpdate.asStateFlow()

    private val _availableUpdate = MutableStateFlow<ReleaseInfo?>(null)
    val availableUpdate = _availableUpdate.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress = _downloadProgress.asStateFlow()

    init {
        checkForUpdate("1.13.0")
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { dataStore.setThemeMode(mode) }
    }

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch { dataStore.setSnoozeDuration(minutes) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { dataStore.setLanguage(lang) }
    }

    fun signInWithGoogle(context: Context, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val authManager = GoogleAuthManager(context)
            val result = authManager.signInWithGoogle()
            if (result.isSuccess) {
                syncCloud {
                    onResult(true, null)
                }
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    fun signOut(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val authManager = GoogleAuthManager(context)
            val result = authManager.signOut()
            onResult(result.isSuccess)
        }
    }

    fun syncCloud(onResult: (SyncResult) -> Unit = {}) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = syncManager.syncNotes()
            _isSyncing.value = false
            if (result is SyncResult.Success) {
                dataStore.setLastSyncTime(System.currentTimeMillis())
            }
            onResult(result)
        }
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
            if (file != null) {
                _availableUpdate.value = null
                _downloadProgress.value = null
                updateManager.installApk(context, file)
            } else {
                _downloadProgress.value = null
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
