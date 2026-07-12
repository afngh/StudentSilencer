package com.afnan.silencer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Manages app settings that need to be saved even when the app is closed.
 */
class PreferenceManager(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val NOTIFY_MODE_CHANGE_KEY = booleanPreferencesKey("notify_mode_change")
        private val RESTORE_ON_RESTART_KEY = booleanPreferencesKey("restore_on_restart")
    }

    // --- DARK MODE ---
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    // --- NOTIFICATIONS ---
    val notifyModeChange: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFY_MODE_CHANGE_KEY] ?: true
    }

    suspend fun setNotifyModeChange(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFY_MODE_CHANGE_KEY] = enabled
        }
    }

    // --- RESTORE ---
    val restoreOnRestart: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[RESTORE_ON_RESTART_KEY] ?: true
    }

    suspend fun setRestoreOnRestart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RESTORE_ON_RESTART_KEY] = enabled
        }
    }
}
