package com.batodev.arrows.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5000L

class AppViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    enum class DebugOption {
        WIDTH, HEIGHT, LIVES, SHAPE
    }

    var shapeProvider: com.batodev.arrows.engine.BoardShapeProvider? = null

    val theme: StateFlow<String> = userPreferencesRepository.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = "Dark"
    )

    val animationSpeed: StateFlow<String> = userPreferencesRepository.animationSpeed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = "Medium"
    )

    val isVibrationEnabled: StateFlow<Boolean> = userPreferencesRepository.isVibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = true
    )

    val isSoundsEnabled: StateFlow<Boolean> = userPreferencesRepository.isSoundsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = true
    )

    val levelNumber: StateFlow<Int> = userPreferencesRepository.levelNumber.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = 1
    )

    val debugForcedWidth: StateFlow<Int?> = userPreferencesRepository.debugForcedWidth.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    val debugForcedHeight: StateFlow<Int?> = userPreferencesRepository.debugForcedHeight.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    val debugForcedLives: StateFlow<Int?> = userPreferencesRepository.debugForcedLives.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    val debugForcedShape: StateFlow<String?> = userPreferencesRepository.debugForcedShape.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    val hasSavedLevel: StateFlow<Boolean> = userPreferencesRepository.currentLevel.map { it != null }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = false
    )

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(theme)
        }
    }

    fun saveVibration(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveVibrationPreference(enabled)
        }
    }

    fun saveSounds(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveSoundsPreference(enabled)
        }
    }

    fun saveAnimationSpeed(speed: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveAnimationSpeed(speed)
        }
    }

    fun saveLevelNumber(level: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveLevelNumber(level)
        }
    }

    fun regenerateCurrentLevel() {
        viewModelScope.launch {
            userPreferencesRepository.clearSavedLevel()
        }
    }

    fun saveDebugOption(option: DebugOption, value: Any?) {
        viewModelScope.launch {
            when (option) {
                DebugOption.WIDTH -> userPreferencesRepository.saveDebugForcedWidth(value as? Int)
                DebugOption.HEIGHT -> userPreferencesRepository.saveDebugForcedHeight(value as? Int)
                DebugOption.LIVES -> userPreferencesRepository.saveDebugForcedLives(value as? Int)
                DebugOption.SHAPE -> userPreferencesRepository.saveDebugForcedShape(value as? String)
            }
        }
    }

    class Factory(private val userPreferencesRepository: UserPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                return AppViewModel(userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
