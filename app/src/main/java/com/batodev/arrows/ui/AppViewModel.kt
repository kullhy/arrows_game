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
