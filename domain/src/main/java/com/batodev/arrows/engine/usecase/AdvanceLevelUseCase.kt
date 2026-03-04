package com.batodev.arrows.engine.usecase

import com.batodev.arrows.data.IGameStateRepository
import com.batodev.arrows.data.IUserPreferencesRepository

/**
 * Advances the player to the next level by incrementing the level counter
 * and clearing the current saved game state.
 */
class AdvanceLevelUseCase(
    private val prefsRepository: IUserPreferencesRepository,
    private val gameStateRepository: IGameStateRepository,
) {
    suspend operator fun invoke(currentLevelNumber: Int) {
        prefsRepository.saveLevelNumber(currentLevelNumber + 1)
        gameStateRepository.clearAllSavedLevels()
    }
}
