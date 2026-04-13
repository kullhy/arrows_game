package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants
import kotlin.random.Random

private const val LEVEL_FACTOR_INTERVAL = 40f
private const val SIZE_FACTOR_WEIGHT = 0.35f
private const val LEVEL_FACTOR_WEIGHT = 0.25f

object ShapeProgressionPolicy {
    fun shouldApplyShape(config: LevelConfiguration, levelNumber: Int, random: Random): Boolean {
        val size = maxOf(config.width, config.height)
        val sizeFactor = when {
            size < GameConstants.MIN_BOARD_SIZE_FOR_SHAPES -> 0f
            size >= GameConstants.MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE -> 1f
            else -> {
                (size - GameConstants.MIN_BOARD_SIZE_FOR_SHAPES).toFloat() /
                    (GameConstants.MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE - GameConstants.MIN_BOARD_SIZE_FOR_SHAPES)
            }
        }

        val levelFactor = ((levelNumber.coerceAtLeast(1) - 1).toFloat() / LEVEL_FACTOR_INTERVAL).coerceIn(0f, 1f)
        val probability = (GameConstants.BASE_SHAPE_PROBABILITY +
                SIZE_FACTOR_WEIGHT * sizeFactor +
                LEVEL_FACTOR_WEIGHT * levelFactor).coerceIn(0f, 1f)

        return random.nextFloat() < probability
    }
}
