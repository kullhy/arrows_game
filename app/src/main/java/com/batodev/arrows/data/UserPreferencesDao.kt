package com.batodev.arrows.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
@Dao
interface UserPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefault(entity: UserPreferencesEntity)

    // Flow queries
    @Query("SELECT theme FROM user_preferences WHERE id = 1")
    fun getTheme(): Flow<String>

    @Query("SELECT animationSpeed FROM user_preferences WHERE id = 1")
    fun getAnimationSpeed(): Flow<String>

    @Query("SELECT isVibrationEnabled FROM user_preferences WHERE id = 1")
    fun getIsVibrationEnabled(): Flow<Boolean>

    @Query("SELECT isSoundsEnabled FROM user_preferences WHERE id = 1")
    fun getIsSoundsEnabled(): Flow<Boolean>

    @Query("SELECT isFillBoardEnabled FROM user_preferences WHERE id = 1")
    fun getIsFillBoardEnabled(): Flow<Boolean>

    @Query("SELECT levelNumber FROM user_preferences WHERE id = 1")
    fun getLevelNumber(): Flow<Int>

    @Query("SELECT currentLives FROM user_preferences WHERE id = 1")
    fun getCurrentLives(): Flow<Int>

    @Query("SELECT debugForcedWidth FROM user_preferences WHERE id = 1")
    fun getDebugForcedWidth(): Flow<Int?>

    @Query("SELECT debugForcedHeight FROM user_preferences WHERE id = 1")
    fun getDebugForcedHeight(): Flow<Int?>

    @Query("SELECT debugForcedLives FROM user_preferences WHERE id = 1")
    fun getDebugForcedLives(): Flow<Int?>

    @Query("SELECT debugForcedShape FROM user_preferences WHERE id = 1")
    fun getDebugForcedShape(): Flow<String?>

    @Query("SELECT isAdFree FROM user_preferences WHERE id = 1")
    fun getIsAdFree(): Flow<Boolean>

    @Query("SELECT rewardAdCount FROM user_preferences WHERE id = 1")
    fun getRewardAdCount(): Flow<Int>

    @Query("SELECT gamesCompleted FROM user_preferences WHERE id = 1")
    fun getGamesCompleted(): Flow<Int>

    @Query("SELECT introCompleted FROM user_preferences WHERE id = 1")
    fun getIntroCompleted(): Flow<Boolean>

    @Query("SELECT isWinVideosEnabled FROM user_preferences WHERE id = 1")
    fun getIsWinVideosEnabled(): Flow<Boolean>

    // Update queries
    @Query("UPDATE user_preferences SET theme = :value WHERE id = 1")
    suspend fun updateTheme(value: String)

    @Query("UPDATE user_preferences SET animationSpeed = :value WHERE id = 1")
    suspend fun updateAnimationSpeed(value: String)

    @Query("UPDATE user_preferences SET isVibrationEnabled = :value WHERE id = 1")
    suspend fun updateVibrationEnabled(value: Boolean)

    @Query("UPDATE user_preferences SET isSoundsEnabled = :value WHERE id = 1")
    suspend fun updateSoundsEnabled(value: Boolean)

    @Query("UPDATE user_preferences SET isFillBoardEnabled = :value WHERE id = 1")
    suspend fun updateFillBoardEnabled(value: Boolean)

    @Query("UPDATE user_preferences SET levelNumber = :value WHERE id = 1")
    suspend fun updateLevelNumber(value: Int)

    @Query("UPDATE user_preferences SET currentLives = :value WHERE id = 1")
    suspend fun updateCurrentLives(value: Int)

    @Query("UPDATE user_preferences SET debugForcedWidth = :value WHERE id = 1")
    suspend fun updateDebugForcedWidth(value: Int?)

    @Query("UPDATE user_preferences SET debugForcedHeight = :value WHERE id = 1")
    suspend fun updateDebugForcedHeight(value: Int?)

    @Query("UPDATE user_preferences SET debugForcedLives = :value WHERE id = 1")
    suspend fun updateDebugForcedLives(value: Int?)

    @Query("UPDATE user_preferences SET debugForcedShape = :value WHERE id = 1")
    suspend fun updateDebugForcedShape(value: String?)

    @Query("UPDATE user_preferences SET isAdFree = :value WHERE id = 1")
    suspend fun updateIsAdFree(value: Boolean)

    @Query("UPDATE user_preferences SET rewardAdCount = rewardAdCount + 1 WHERE id = 1")
    suspend fun incrementRewardAdCount()

    @Query("UPDATE user_preferences SET rewardAdCount = 0 WHERE id = 1")
    suspend fun resetRewardAdCount()

    @Query("UPDATE user_preferences SET gamesCompleted = gamesCompleted + 1 WHERE id = 1")
    suspend fun incrementGamesCompleted()

    @Query("UPDATE user_preferences SET introCompleted = :value WHERE id = 1")
    suspend fun updateIntroCompleted(value: Boolean)

    @Query("UPDATE user_preferences SET isWinVideosEnabled = :value WHERE id = 1")
    suspend fun updateWinVideosEnabled(value: Boolean)
}
