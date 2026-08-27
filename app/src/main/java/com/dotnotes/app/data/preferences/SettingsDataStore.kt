package com.dotnotes.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SNOOZE_DURATION = intPreferencesKey("snooze_duration")
    }

    val themeMode: Flow<String> = context.dataStore.data.map {
        it[THEME_MODE] ?: "system"
    }

    val snoozeDuration: Flow<Int> = context.dataStore.data.map {
        it[SNOOZE_DURATION] ?: 5
    }

    val googleEmail: Flow<String?> = context.dataStore.data.map {
        it[stringPreferencesKey("google_email")]
    }


    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { it[SNOOZE_DURATION] = minutes }
    }

    suspend fun setGoogleEmail(email: String?) {
        context.dataStore.edit { prefs ->
            if (email == null) {
                prefs.remove(stringPreferencesKey("google_email"))
            } else {
                prefs[stringPreferencesKey("google_email")] = email
            }
        }
    }
}
