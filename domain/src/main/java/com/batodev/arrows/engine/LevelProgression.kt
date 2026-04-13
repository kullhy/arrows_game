package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants

private const val EXTRA_SIZE_REDUCTION_INTERVAL = 15
private const val EXTRA_LIVES_REDUCTION_INTERVAL = 12
private const val LEVEL_SIZE_INCREMENT_INTERVAL = 2
private const val MIN_BOARD_SIZE = 4
private const val EXTRA_SNAKE_LENGTH_INTERVAL = 8

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
        val extraSizeReduction = safeLevel / EXTRA_SIZE_REDUCTION_INTERVAL
        val livesReduction = progressionStep * GameConstants.LIVES_REDUCTION_PER_STEP +
                safeLevel / EXTRA_LIVES_REDUCTION_INTERVAL

        val baseH = GameConstants.BASE_BOARD_SIZE + (safeLevel - 1) / LEVEL_SIZE_INCREMENT_INTERVAL
        val baseW = GameConstants.BASE_BOARD_SIZE + safeLevel / LEVEL_SIZE_INCREMENT_INTERVAL

        val h = forcedHeight ?: (baseH - sizeReduction - extraSizeReduction).coerceAtLeast(MIN_BOARD_SIZE)
        val w = forcedWidth ?: (baseW - sizeReduction - extraSizeReduction).coerceAtLeast(MIN_BOARD_SIZE)

        val maxLives = forcedLives
            ?: (GameConstants.DEFAULT_INITIAL_LIVES - livesReduction).coerceIn(1, GameConstants.DEFAULT_INITIAL_LIVES)
        val snakeLen = (GameConstants.MIN_SNAKE_LENGTH_BASE +
                safeLevel / LEVEL_SIZE_INCREMENT_INTERVAL +
                safeLevel / EXTRA_SNAKE_LENGTH_INTERVAL)
            .coerceIn(GameConstants.MIN_SNAKE_LENGTH_MIN, GameConstants.MIN_SNAKE_LENGTH_MAX)

        return LevelConfiguration(w, h, snakeLen, maxLives)
    }
}
