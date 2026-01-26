package com.batodev.arrows.engine

import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository : UserPreferencesRepository(
    androidx.datastore.core.DataStoreFactory.create(
        serializer = object : androidx.datastore.core.Serializer<androidx.datastore.preferences.core.Preferences> {
            override val defaultValue: androidx.datastore.preferences.core.Preferences
                get() = androidx.datastore.preferences.core.emptyPreferences()
            override suspend fun readFrom(input: java.io.InputStream): androidx.datastore.preferences.core.Preferences = defaultValue
            override suspend fun writeTo(t: androidx.datastore.preferences.core.Preferences, output: java.io.OutputStream) {}
        }
    ) { null!! }
) {
    val themeFlow = MutableStateFlow("Dark")
    override val theme: Flow<String> = themeFlow

    val vibrationFlow = MutableStateFlow(true)
    override val isVibrationEnabled: Flow<Boolean> = vibrationFlow

    val soundsFlow = MutableStateFlow(true)
    override val isSoundsEnabled: Flow<Boolean> = soundsFlow

    val fillBoardFlow = MutableStateFlow(false)
    override val isFillBoardEnabled: Flow<Boolean> = fillBoardFlow

    val animationSpeedFlow = MutableStateFlow("Medium")
    override val animationSpeed: Flow<String> = animationSpeedFlow

    val levelNumberFlow = MutableStateFlow(1)
    override val levelNumber: Flow<Int> = levelNumberFlow

    val currentLivesFlow = MutableStateFlow(5)
    override val currentLives: Flow<Int> = currentLivesFlow

    val initialLevelFlow = MutableStateFlow<String?>(null)
    override val initialLevel: Flow<String?> = initialLevelFlow

    val currentLevelFlow = MutableStateFlow<String?>(null)
    override val currentLevel: Flow<String?> = currentLevelFlow

    val debugForcedWidthFlow = MutableStateFlow<Int?>(null)
    override val debugForcedWidth: Flow<Int?> = debugForcedWidthFlow

    val debugForcedHeightFlow = MutableStateFlow<Int?>(null)
    override val debugForcedHeight: Flow<Int?> = debugForcedHeightFlow

    val debugForcedLivesFlow = MutableStateFlow<Int?>(null)
    override val debugForcedLives: Flow<Int?> = debugForcedLivesFlow

    val debugForcedShapeFlow = MutableStateFlow<String?>(null)
    override val debugForcedShape: Flow<String?> = debugForcedShapeFlow

    override suspend fun saveThemePreference(theme: String) {
        themeFlow.value = theme
    }

    override suspend fun saveVibrationPreference(enabled: Boolean) {
        vibrationFlow.value = enabled
    }

    override suspend fun saveSoundsPreference(enabled: Boolean) {
        soundsFlow.value = enabled
    }

    override suspend fun saveFillBoardPreference(enabled: Boolean) {
        fillBoardFlow.value = enabled
    }

    override suspend fun saveAnimationSpeed(speed: String) {
        animationSpeedFlow.value = speed
    }

    override suspend fun saveLevelNumber(level: Int) {
        levelNumberFlow.value = level
    }

    override suspend fun saveCurrentLives(lives: Int) {
        currentLivesFlow.value = lives
    }

    override suspend fun saveInitialLevel(levelJson: String) {
        initialLevelFlow.value = levelJson
    }

    override suspend fun saveCurrentLevel(levelJson: String) {
        currentLevelFlow.value = levelJson
    }

    override suspend fun saveDebugForcedWidth(width: Int?) {
        debugForcedWidthFlow.value = width
    }

    override suspend fun saveDebugForcedHeight(height: Int?) {
        debugForcedHeightFlow.value = height
    }

    override suspend fun saveDebugForcedLives(lives: Int?) {
        debugForcedLivesFlow.value = lives
    }

    override suspend fun saveDebugForcedShape(shape: String?) {
        debugForcedShapeFlow.value = shape
    }

    override suspend fun clearSavedLevel() {
        initialLevelFlow.value = null
        currentLevelFlow.value = null
    }
}