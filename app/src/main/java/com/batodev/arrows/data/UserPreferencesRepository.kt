package com.batodev.arrows.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

open class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val INITIAL_LEVEL = stringPreferencesKey("initial_level")
        val CURRENT_LEVEL = stringPreferencesKey("current_level")
        val VIBRATION_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("vibration_enabled")
    }

    open val theme: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME] ?: "Dark"
        }

    open val isVibrationEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
        }

    open val initialLevel: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.INITIAL_LEVEL] }

    open val currentLevel: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_LEVEL] }

    open suspend fun saveThemePreference(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    open suspend fun saveVibrationPreference(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    open suspend fun saveInitialLevel(levelJson: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.INITIAL_LEVEL] = levelJson
        }
    }

    open suspend fun saveCurrentLevel(levelJson: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LEVEL] = levelJson
        }
    }

    open suspend fun clearSavedLevel() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.INITIAL_LEVEL)
            preferences.remove(PreferencesKeys.CURRENT_LEVEL)
        }
    }
}
