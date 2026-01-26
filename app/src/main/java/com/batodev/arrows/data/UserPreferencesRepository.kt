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
        val ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        val SOUNDS_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("sounds_enabled")
        val FILL_BOARD_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("fill_board_enabled")
        val LEVEL_NUMBER = androidx.datastore.preferences.core.intPreferencesKey("level_number")
        val CURRENT_LIVES = androidx.datastore.preferences.core.intPreferencesKey("current_lives")
        val DEBUG_FORCED_WIDTH = androidx.datastore.preferences.core.intPreferencesKey("debug_forced_width")
        val DEBUG_FORCED_HEIGHT = androidx.datastore.preferences.core.intPreferencesKey("debug_forced_height")
        val DEBUG_FORCED_LIVES = androidx.datastore.preferences.core.intPreferencesKey("debug_forced_lives")
        val DEBUG_FORCED_SHAPE = stringPreferencesKey("debug_forced_shape")
    }

    open val theme: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME] ?: "Dark"
        }

    open val animationSpeed: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ANIMATION_SPEED] ?: "Medium"
        }

    open val isVibrationEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
        }

    open val isSoundsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SOUNDS_ENABLED] ?: true
        }

    open val isFillBoardEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FILL_BOARD_ENABLED] ?: false
        }

    open val levelNumber: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LEVEL_NUMBER] ?: 1
        }

    open val currentLives: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_LIVES] ?: 5
        }

    open val debugForcedWidth: Flow<Int?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DEBUG_FORCED_WIDTH] }

    open val debugForcedHeight: Flow<Int?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DEBUG_FORCED_HEIGHT] }

    open val debugForcedLives: Flow<Int?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DEBUG_FORCED_LIVES] }

    open val debugForcedShape: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DEBUG_FORCED_SHAPE] }

    open val initialLevel: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.INITIAL_LEVEL] }

    open val currentLevel: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_LEVEL] }

    open suspend fun saveThemePreference(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    open suspend fun saveAnimationSpeed(speed: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATION_SPEED] = speed
        }
    }

    open suspend fun saveVibrationPreference(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    open suspend fun saveSoundsPreference(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUNDS_ENABLED] = enabled
        }
    }

    open suspend fun saveFillBoardPreference(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILL_BOARD_ENABLED] = enabled
        }
    }

    open suspend fun saveLevelNumber(level: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LEVEL_NUMBER] = level
        }
    }

    open suspend fun saveCurrentLives(lives: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LIVES] = lives
        }
    }

    open suspend fun saveDebugForcedWidth(width: Int?) {
        dataStore.edit { preferences ->
            if (width == null) preferences.remove(PreferencesKeys.DEBUG_FORCED_WIDTH)
            else preferences[PreferencesKeys.DEBUG_FORCED_WIDTH] = width
        }
    }

    open suspend fun saveDebugForcedHeight(height: Int?) {
        dataStore.edit { preferences ->
            if (height == null) preferences.remove(PreferencesKeys.DEBUG_FORCED_HEIGHT)
            else preferences[PreferencesKeys.DEBUG_FORCED_HEIGHT] = height
        }
    }

    open suspend fun saveDebugForcedLives(lives: Int?) {
        dataStore.edit { preferences ->
            if (lives == null) preferences.remove(PreferencesKeys.DEBUG_FORCED_LIVES)
            else preferences[PreferencesKeys.DEBUG_FORCED_LIVES] = lives
        }
    }

    open suspend fun saveDebugForcedShape(shape: String?) {
        dataStore.edit { preferences ->
            if (shape == null) preferences.remove(PreferencesKeys.DEBUG_FORCED_SHAPE)
            else preferences[PreferencesKeys.DEBUG_FORCED_SHAPE] = shape
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
