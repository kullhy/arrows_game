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

class UserPreferencesRepository(private val context: Context) {
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val INITIAL_LEVEL = stringPreferencesKey("initial_level")
        val CURRENT_LEVEL = stringPreferencesKey("current_level")
    }

    val theme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME] ?: "Dark"
        }

    val initialLevel: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.INITIAL_LEVEL] }

    val currentLevel: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_LEVEL] }

    suspend fun saveThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    suspend fun saveInitialLevel(levelJson: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INITIAL_LEVEL] = levelJson
        }
    }

    suspend fun saveCurrentLevel(levelJson: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LEVEL] = levelJson
        }
    }
}
