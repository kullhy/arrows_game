package com.batodev.arrows.engine

private const val BASE_BOARD_SIZE = 5
private const val SIZE_REDUCTION_PER_STEP = 3
private const val LIVES_REDUCTION_PER_STEP = 1
private const val LEVELS_PER_PROGRESSION_STEP = 10
private const val DEFAULT_INITIAL_LIVES = 5
private const val MIN_SNAKE_LENGTH_BASE = 3
private const val MIN_SNAKE_LENGTH_MIN = 4
private const val MIN_SNAKE_LENGTH_MAX = 30

object LevelProgression {
    fun calculateLevelConfiguration(
        levelNum: Int,
        forcedWidth: Int? = null,
        forcedHeight: Int? = null,
        forcedLives: Int? = null
    ): LevelConfiguration {
        val progressionStep = levelNum / LEVELS_PER_PROGRESSION_STEP
        val sizeReduction = progressionStep * SIZE_REDUCTION_PER_STEP
        val livesReduction = progressionStep * LIVES_REDUCTION_PER_STEP

        val baseH = BASE_BOARD_SIZE + (levelNum - 1) / 2
        val baseW = BASE_BOARD_SIZE + levelNum / 2

        val h = forcedHeight ?: (baseH - sizeReduction).coerceAtLeast(1)
        val w = forcedWidth ?: (baseW - sizeReduction).coerceAtLeast(1)

        val maxLives = forcedLives ?: (DEFAULT_INITIAL_LIVES - livesReduction).coerceAtLeast(1)
        val snakeLen = (MIN_SNAKE_LENGTH_BASE + levelNum / 2).coerceIn(MIN_SNAKE_LENGTH_MIN, MIN_SNAKE_LENGTH_MAX)

        return LevelConfiguration(w, h, snakeLen, maxLives)
    }
}
