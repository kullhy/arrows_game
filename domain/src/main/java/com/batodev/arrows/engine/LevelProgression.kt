package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants

object LevelProgression {
    fun calculateLevelConfiguration(
        levelNum: Int,
        forcedWidth: Int? = null,
        forcedHeight: Int? = null,
        forcedLives: Int? = null
    ): LevelConfiguration {
        val safeLevel = levelNum.coerceAtLeast(1)
        val progressionStep = safeLevel / GameConstants.LEVELS_PER_PROGRESSION_STEP
        val sizeReduction = progressionStep * GameConstants.SIZE_REDUCTION_PER_STEP
        val extraSizeReduction = safeLevel / 15
        val livesReduction = progressionStep * GameConstants.LIVES_REDUCTION_PER_STEP + safeLevel / 12

        val baseH = GameConstants.BASE_BOARD_SIZE + (safeLevel - 1) / 2
        val baseW = GameConstants.BASE_BOARD_SIZE + safeLevel / 2

        val h = forcedHeight ?: (baseH - sizeReduction - extraSizeReduction).coerceAtLeast(4)
        val w = forcedWidth ?: (baseW - sizeReduction - extraSizeReduction).coerceAtLeast(4)

        val maxLives = forcedLives
            ?: (GameConstants.DEFAULT_INITIAL_LIVES - livesReduction).coerceIn(1, GameConstants.DEFAULT_INITIAL_LIVES)
        val snakeLen = (GameConstants.MIN_SNAKE_LENGTH_BASE + safeLevel / 2 + safeLevel / 8)
            .coerceIn(GameConstants.MIN_SNAKE_LENGTH_MIN, GameConstants.MIN_SNAKE_LENGTH_MAX)

        return LevelConfiguration(w, h, snakeLen, maxLives)
    }
}
