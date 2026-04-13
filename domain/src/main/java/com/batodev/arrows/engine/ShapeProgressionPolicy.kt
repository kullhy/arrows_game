package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants
import kotlin.random.Random

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

        val levelFactor = ((levelNumber.coerceAtLeast(1) - 1).toFloat() / 40f).coerceIn(0f, 1f)
        val probability = (GameConstants.BASE_SHAPE_PROBABILITY + 0.35f * sizeFactor + 0.25f * levelFactor)
            .coerceIn(0f, 1f)

        return random.nextFloat() < probability
    }
}
