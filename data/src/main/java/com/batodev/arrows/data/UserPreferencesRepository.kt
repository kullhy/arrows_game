package com.batodev.arrows.data

import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.flow.Flow

// Room DAOs and preference repositories mirror a single DB table's columns;
// splitting them across multiple classes would add coordination overhead
// with no architectural benefit for a single-row preferences entity.
@Suppress("TooManyFunctions")
class UserPreferencesRepository(private val dao: UserPreferencesDao) : IUserPreferencesRepository {

    override val theme: Flow<String> get() = dao.getTheme()
    override val animationSpeed: Flow<String> get() = dao.getAnimationSpeed()
    override val isVibrationEnabled: Flow<Boolean> get() = dao.getIsVibrationEnabled()
    override val isSoundsEnabled: Flow<Boolean> get() = dao.getIsSoundsEnabled()
    override val isFillBoardEnabled: Flow<Boolean> get() = dao.getIsFillBoardEnabled()
    override val levelNumber: Flow<Int> get() = dao.getLevelNumber()
    override val currentLives: Flow<Int> get() = dao.getCurrentLives()
    override val debugForcedWidth: Flow<Int?> get() = dao.getDebugForcedWidth()
    override val debugForcedHeight: Flow<Int?> get() = dao.getDebugForcedHeight()
    override val debugForcedLives: Flow<Int?> get() = dao.getDebugForcedLives()
    override val debugForcedShape: Flow<String?> get() = dao.getDebugForcedShape()
    override val isAdFree: Flow<Boolean> get() = dao.getIsAdFree()
    override val rewardAdCount: Flow<Int> get() = dao.getRewardAdCount()
    override val gamesCompleted: Flow<Int> get() = dao.getGamesCompleted()
    override val introCompleted: Flow<Boolean> get() = dao.getIntroCompleted()
    override val isWinVideosEnabled: Flow<Boolean> get() = dao.getIsWinVideosEnabled()

    override suspend fun saveThemePreference(theme: String) = dao.updateTheme(theme)
    override suspend fun saveAnimationSpeed(speed: String) = dao.updateAnimationSpeed(speed)
    override suspend fun saveVibrationPreference(enabled: Boolean) = dao.updateVibrationEnabled(enabled)
    override suspend fun saveSoundsPreference(enabled: Boolean) = dao.updateSoundsEnabled(enabled)
    override suspend fun saveFillBoardPreference(enabled: Boolean) = dao.updateFillBoardEnabled(enabled)
    override suspend fun saveLevelNumber(level: Int) = dao.updateLevelNumber(level)
    override suspend fun saveCurrentLives(lives: Int) = dao.updateCurrentLives(lives)
    override suspend fun saveDebugForcedWidth(width: Int?) = dao.updateDebugForcedWidth(width)
    override suspend fun saveDebugForcedHeight(height: Int?) = dao.updateDebugForcedHeight(height)
    override suspend fun saveDebugForcedLives(lives: Int?) = dao.updateDebugForcedLives(lives)
    override suspend fun saveDebugForcedShape(shape: String?) = dao.updateDebugForcedShape(shape)
    override suspend fun saveIsAdFree(isAdFree: Boolean) = dao.updateIsAdFree(isAdFree)
    override suspend fun incrementRewardAdCount() = dao.incrementRewardAdCount()
    override suspend fun resetRewardAdCount() = dao.resetRewardAdCount()
    override suspend fun saveIntroCompleted(completed: Boolean) = dao.updateIntroCompleted(completed)
    override suspend fun saveWinVideosEnabled(enabled: Boolean) = dao.updateWinVideosEnabled(enabled)
    override suspend fun incrementGamesCompleted() = dao.incrementGamesCompleted()
}
