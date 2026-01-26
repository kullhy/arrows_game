package com.batodev.arrows.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    var shapeProvider: com.batodev.arrows.engine.BoardShapeProvider? = null

    val theme: StateFlow<String> = userPreferencesRepository.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Dark"
    )

    val animationSpeed: StateFlow<String> = userPreferencesRepository.animationSpeed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Medium"
    )

    val isVibrationEnabled: StateFlow<Boolean> = userPreferencesRepository.isVibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val isSoundsEnabled: StateFlow<Boolean> = userPreferencesRepository.isSoundsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val isFillBoardEnabled: StateFlow<Boolean> = userPreferencesRepository.isFillBoardEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val levelNumber: StateFlow<Int> = userPreferencesRepository.levelNumber.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1
    )

    val debugForcedWidth: StateFlow<Int?> = userPreferencesRepository.debugForcedWidth.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val debugForcedHeight: StateFlow<Int?> = userPreferencesRepository.debugForcedHeight.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val debugForcedLives: StateFlow<Int?> = userPreferencesRepository.debugForcedLives.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val debugForcedShape: StateFlow<String?> = userPreferencesRepository.debugForcedShape.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
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

    fun saveFillBoard(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveFillBoardPreference(enabled)
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

    fun saveDebugForcedWidth(width: Int?) {
        viewModelScope.launch {
            userPreferencesRepository.saveDebugForcedWidth(width)
        }
    }

    fun saveDebugForcedHeight(height: Int?) {
        viewModelScope.launch {
            userPreferencesRepository.saveDebugForcedHeight(height)
        }
    }

    fun saveDebugForcedLives(lives: Int?) {
        viewModelScope.launch {
            userPreferencesRepository.saveDebugForcedLives(lives)
        }
    }

    fun saveDebugForcedShape(shape: String?) {
        viewModelScope.launch {
            userPreferencesRepository.saveDebugForcedShape(shape)
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
