package com.batodev.arrows.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.io.File

private val Context.legacyDataStore by preferencesDataStore(name = "settings")

private const val TAG = "DataStoreMigration"

suspend fun migrateFromDataStoreIfNeeded(context: Context, dao: UserPreferencesDao) {
    val dataStoreFile = File(context.filesDir, "datastore/settings.preferences_pb")
    if (!dataStoreFile.exists()) return

    Log.i(TAG, "Found legacy DataStore file, starting migration")

    try {
        val prefs: Preferences = context.legacyDataStore.data.first()

        val entity = UserPreferencesEntity(
            id = 1,
            theme = prefs[stringPreferencesKey("theme")] ?: "Green",
            animationSpeed = prefs[stringPreferencesKey("animation_speed")] ?: "Medium",
            isVibrationEnabled = prefs[booleanPreferencesKey("vibration_enabled")] ?: true,
            isSoundsEnabled = prefs[booleanPreferencesKey("sounds_enabled")] ?: true,
            isFillBoardEnabled = prefs[booleanPreferencesKey("fill_board_enabled")] ?: false,
            levelNumber = prefs[intPreferencesKey("level_number")] ?: 1,
            currentLives = prefs[intPreferencesKey("current_lives")] ?: 5,
            debugForcedWidth = prefs[intPreferencesKey("debug_forced_width")],
            debugForcedHeight = prefs[intPreferencesKey("debug_forced_height")],
            debugForcedLives = prefs[intPreferencesKey("debug_forced_lives")],
            debugForcedShape = prefs[stringPreferencesKey("debug_forced_shape")],
            isAdFree = prefs[booleanPreferencesKey("is_ad_free")] ?: false,
            rewardAdCount = prefs[intPreferencesKey("reward_ad_count")] ?: 0,
            gamesCompleted = prefs[intPreferencesKey("games_completed")] ?: 0,
            introCompleted = prefs[booleanPreferencesKey("intro_completed")] ?: false,
            isWinVideosEnabled = prefs[booleanPreferencesKey("win_videos_enabled")] ?: false
        )

        dao.upsert(entity)
        Log.i(TAG, "Migration complete: levelNumber=${entity.levelNumber}, gamesCompleted=${entity.gamesCompleted}")

        // Clean up old DataStore files
        dataStoreFile.delete()
        val dataStoreDir = File(context.filesDir, "datastore")
        if (dataStoreDir.isDirectory && dataStoreDir.listFiles().isNullOrEmpty()) {
            dataStoreDir.delete()
        }

        Log.i(TAG, "Old DataStore files cleaned up")
    } catch (e: Exception) {
        Log.e(TAG, "DataStore migration failed, keeping old file for retry", e)
    }
}
