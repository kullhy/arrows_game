package com.batodev.arrows.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batodev.arrows.GameConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Suppress("TooManyFunctions")
open class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val INITIAL_LEVEL = stringPreferencesKey("initial_level")
        val CURRENT_LEVEL = stringPreferencesKey("current_level")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        val SOUNDS_ENABLED = booleanPreferencesKey("sounds_enabled")
        val FILL_BOARD_ENABLED = booleanPreferencesKey("fill_board_enabled")
        val LEVEL_NUMBER = intPreferencesKey("level_number")
        val CURRENT_LIVES = intPreferencesKey("current_lives")
        val DEBUG_FORCED_WIDTH = intPreferencesKey("debug_forced_width")
        val DEBUG_FORCED_HEIGHT = intPreferencesKey("debug_forced_height")
        val DEBUG_FORCED_LIVES = intPreferencesKey("debug_forced_lives")
        val DEBUG_FORCED_SHAPE = stringPreferencesKey("debug_forced_shape")
        val IS_AD_FREE = booleanPreferencesKey("is_ad_free")
        val REWARD_AD_COUNT = intPreferencesKey("reward_ad_count")
        val GAMES_COMPLETED = intPreferencesKey("games_completed")
        val INTRO_COMPLETED = booleanPreferencesKey("intro_completed")
        val WIN_VIDEOS_ENABLED = booleanPreferencesKey("win_videos_enabled")
    }

    open val theme: Flow<String> get() = getStringFlow(PreferencesKeys.THEME, "Green")
    open val animationSpeed: Flow<String> get() = getStringFlow(PreferencesKeys.ANIMATION_SPEED, "Medium")
    open val isVibrationEnabled: Flow<Boolean> get() = getBooleanFlow(PreferencesKeys.VIBRATION_ENABLED, true)
    open val isSoundsEnabled: Flow<Boolean> get() = getBooleanFlow(PreferencesKeys.SOUNDS_ENABLED, true)
    open val isFillBoardEnabled: Flow<Boolean> get() = getBooleanFlow(PreferencesKeys.FILL_BOARD_ENABLED, false)
    open val levelNumber: Flow<Int> get() = getIntFlow(PreferencesKeys.LEVEL_NUMBER, GameConstants.DEFAULT_LEVEL)
    open val currentLives: Flow<Int> get() = getIntFlow(PreferencesKeys.CURRENT_LIVES, GameConstants.DEFAULT_LIVES)
    open val debugForcedWidth: Flow<Int?> get() = getNullableIntFlow(PreferencesKeys.DEBUG_FORCED_WIDTH)
    open val debugForcedHeight: Flow<Int?> get() = getNullableIntFlow(PreferencesKeys.DEBUG_FORCED_HEIGHT)
    open val debugForcedLives: Flow<Int?> get() = getNullableIntFlow(PreferencesKeys.DEBUG_FORCED_LIVES)
    open val debugForcedShape: Flow<String?> get() = getNullableStringFlow(PreferencesKeys.DEBUG_FORCED_SHAPE)
    open val initialLevel: Flow<String?> get() = getNullableStringFlow(PreferencesKeys.INITIAL_LEVEL)
    open val currentLevel: Flow<String?> get() = getNullableStringFlow(PreferencesKeys.CURRENT_LEVEL)
    open val isAdFree: Flow<Boolean> get() = getBooleanFlow(PreferencesKeys.IS_AD_FREE, false)
    open val rewardAdCount: Flow<Int> get() = getIntFlow(PreferencesKeys.REWARD_AD_COUNT, 0)
    open val gamesCompleted: Flow<Int> get() = getIntFlow(PreferencesKeys.GAMES_COMPLETED, 0)
    open val introCompleted: Flow<Boolean> get() = getBooleanFlow(PreferencesKeys.INTRO_COMPLETED, false)
    open val isWinVideosEnabled: Flow<Boolean> get() = getBooleanFlow(PreferencesKeys.WIN_VIDEOS_ENABLED, true)

    open suspend fun saveThemePreference(theme: String) = save(PreferencesKeys.THEME, theme)
    open suspend fun saveAnimationSpeed(speed: String) = save(PreferencesKeys.ANIMATION_SPEED, speed)
    open suspend fun saveVibrationPreference(enabled: Boolean) = save(PreferencesKeys.VIBRATION_ENABLED, enabled)
    open suspend fun saveSoundsPreference(enabled: Boolean) = save(PreferencesKeys.SOUNDS_ENABLED, enabled)
    open suspend fun saveFillBoardPreference(enabled: Boolean) = save(PreferencesKeys.FILL_BOARD_ENABLED, enabled)
    open suspend fun saveLevelNumber(level: Int) = save(PreferencesKeys.LEVEL_NUMBER, level)
    open suspend fun saveCurrentLives(lives: Int) = save(PreferencesKeys.CURRENT_LIVES, lives)
    open suspend fun saveDebugForcedWidth(width: Int?) = saveNullable(PreferencesKeys.DEBUG_FORCED_WIDTH, width)
    open suspend fun saveDebugForcedHeight(height: Int?) = saveNullable(PreferencesKeys.DEBUG_FORCED_HEIGHT, height)
    open suspend fun saveDebugForcedLives(lives: Int?) = saveNullable(PreferencesKeys.DEBUG_FORCED_LIVES, lives)
    open suspend fun saveDebugForcedShape(shape: String?) = saveNullable(PreferencesKeys.DEBUG_FORCED_SHAPE, shape)
    open suspend fun saveInitialLevel(levelJson: String) = save(PreferencesKeys.INITIAL_LEVEL, levelJson)
    open suspend fun saveCurrentLevel(levelJson: String) = save(PreferencesKeys.CURRENT_LEVEL, levelJson)

    open suspend fun clearSavedLevel() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.INITIAL_LEVEL)
            preferences.remove(PreferencesKeys.CURRENT_LEVEL)
        }
    }

    open suspend fun saveIsAdFree(isAdFree: Boolean) = save(PreferencesKeys.IS_AD_FREE, isAdFree)

    open suspend fun incrementRewardAdCount() {
        dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.REWARD_AD_COUNT] ?: 0
            preferences[PreferencesKeys.REWARD_AD_COUNT] = currentCount + 1
        }
    }

    open suspend fun resetRewardAdCount() = save(PreferencesKeys.REWARD_AD_COUNT, 0)

    open suspend fun saveIntroCompleted(completed: Boolean) = save(PreferencesKeys.INTRO_COMPLETED, completed)
    open suspend fun saveWinVideosEnabled(enabled: Boolean) = save(PreferencesKeys.WIN_VIDEOS_ENABLED, enabled)

    open suspend fun incrementGamesCompleted() {
        dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.GAMES_COMPLETED] ?: 0
            preferences[PreferencesKeys.GAMES_COMPLETED] = currentCount + 1
        }
    }

    private fun <T> getFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataStore.data.map { it[key] ?: defaultValue }

    private fun <T> getNullableFlow(key: Preferences.Key<T>): Flow<T?> =
        dataStore.data.map { it[key] }

    private fun getStringFlow(key: Preferences.Key<String>, defaultValue: String) = getFlow(key, defaultValue)
    private fun getBooleanFlow(key: Preferences.Key<Boolean>, defaultValue: Boolean) = getFlow(key, defaultValue)
    private fun getIntFlow(key: Preferences.Key<Int>, defaultValue: Int) = getFlow(key, defaultValue)
    private fun getNullableStringFlow(key: Preferences.Key<String>) = getNullableFlow(key)
    private fun getNullableIntFlow(key: Preferences.Key<Int>) = getNullableFlow(key)

    private suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    private suspend fun <T> saveNullable(key: Preferences.Key<T>, value: T?) {
        dataStore.edit { if (value == null) it.remove(key) else it[key] = value }
    }
}
