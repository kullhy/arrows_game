package com.batodev.arrows.data

import kotlinx.coroutines.flow.Flow

interface IGameStateRepository {
    fun hasSavedLevel(): Flow<Boolean>
    suspend fun saveGameLevel(stateType: String, width: Int, height: Int, snakes: List<SnakeSaveData>)
    suspend fun loadGameLevel(stateType: String): GameLevelData?
    suspend fun clearAllSavedLevels()
}

// Room DAOs and preference repositories mirror a single DB table's columns;
// splitting them across multiple classes would add coordination overhead
// with no architectural benefit for a single-row preferences entity.
@Suppress("TooManyFunctions")
interface IUserPreferencesRepository {
    val theme: Flow<String>
    val animationSpeed: Flow<String>
    val isVibrationEnabled: Flow<Boolean>
    val isSoundsEnabled: Flow<Boolean>
    val isFillBoardEnabled: Flow<Boolean>
    val levelNumber: Flow<Int>
    val currentLives: Flow<Int>
    val debugForcedWidth: Flow<Int?>
    val debugForcedHeight: Flow<Int?>
    val debugForcedLives: Flow<Int?>
    val debugForcedShape: Flow<String?>
    val isAdFree: Flow<Boolean>
    val rewardAdCount: Flow<Int>
    val gamesCompleted: Flow<Int>
    val introCompleted: Flow<Boolean>
    val isWinVideosEnabled: Flow<Boolean>

    suspend fun saveThemePreference(theme: String)
    suspend fun saveAnimationSpeed(speed: String)
    suspend fun saveVibrationPreference(enabled: Boolean)
    suspend fun saveSoundsPreference(enabled: Boolean)
    suspend fun saveFillBoardPreference(enabled: Boolean)
    suspend fun saveLevelNumber(level: Int)
    suspend fun saveCurrentLives(lives: Int)
    suspend fun saveDebugForcedWidth(width: Int?)
    suspend fun saveDebugForcedHeight(height: Int?)
    suspend fun saveDebugForcedLives(lives: Int?)
    suspend fun saveDebugForcedShape(shape: String?)
    suspend fun saveIsAdFree(isAdFree: Boolean)
    suspend fun incrementRewardAdCount()
    suspend fun resetRewardAdCount()
    suspend fun saveIntroCompleted(completed: Boolean)
    suspend fun saveWinVideosEnabled(enabled: Boolean)
    suspend fun incrementGamesCompleted()
}
