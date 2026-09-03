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
        val LANGUAGE = stringPreferencesKey("language")
        val LAST_SYNC_TIME = androidx.datastore.preferences.core.longPreferencesKey("last_sync_time")
        val DISMISSED_UPDATE_TAG = stringPreferencesKey("dismissed_update_tag")
        val REMINDER_SOUND_URI = stringPreferencesKey("reminder_sound_uri")
        val REMINDER_SOUND_TITLE = stringPreferencesKey("reminder_sound_title")
        val ALARM_SOUND_URI = stringPreferencesKey("alarm_sound_uri")
        val ALARM_SOUND_TITLE = stringPreferencesKey("alarm_sound_title")
    }

    val themeMode: Flow<String> = context.dataStore.data.map {
        it[THEME_MODE] ?: "system"
    }

    val snoozeDuration: Flow<Int> = context.dataStore.data.map {
        it[SNOOZE_DURATION] ?: 5
    }

    val language: Flow<String> = context.dataStore.data.map {
        it[LANGUAGE] ?: "en"
    }

    val lastSyncTime: Flow<Long> = context.dataStore.data.map {
        it[LAST_SYNC_TIME] ?: 0L
    }

    val dismissedUpdateTag: Flow<String> = context.dataStore.data.map {
        it[DISMISSED_UPDATE_TAG] ?: ""
    }

    val reminderSoundUri: Flow<String> = context.dataStore.data.map {
        it[REMINDER_SOUND_URI] ?: ""
    }

    val reminderSoundTitle: Flow<String> = context.dataStore.data.map {
        it[REMINDER_SOUND_TITLE] ?: "Default"
    }

    val alarmSoundUri: Flow<String> = context.dataStore.data.map {
        it[ALARM_SOUND_URI] ?: ""
    }

    val alarmSoundTitle: Flow<String> = context.dataStore.data.map {
        it[ALARM_SOUND_TITLE] ?: "Default"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { it[SNOOZE_DURATION] = minutes }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setLastSyncTime(time: Long) {
        context.dataStore.edit { it[LAST_SYNC_TIME] = time }
    }

    suspend fun setDismissedUpdateTag(tag: String) {
        context.dataStore.edit { it[DISMISSED_UPDATE_TAG] = tag }
    }

    suspend fun setReminderSound(uri: String, title: String) {
        context.dataStore.edit {
            it[REMINDER_SOUND_URI] = uri
            it[REMINDER_SOUND_TITLE] = title
        }
    }

    suspend fun setAlarmSound(uri: String, title: String) {
        context.dataStore.edit {
            it[ALARM_SOUND_URI] = uri
            it[ALARM_SOUND_TITLE] = title
        }
    }
}
