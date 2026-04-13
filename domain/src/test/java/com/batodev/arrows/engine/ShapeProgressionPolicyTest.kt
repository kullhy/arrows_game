package com.batodev.arrows.engine

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ShapeProgressionPolicyTest {
    @Test
    fun `shape probability increases with level`() {
        val low = estimateProbability(level = 1, size = 24)
        val high = estimateProbability(level = 40, size = 24)

        assertTrue("Expected higher shape probability at later levels", high > low)
    }

    @Test
    fun `shape probability increases with board size`() {
        val small = estimateProbability(level = 20, size = 22)
        val large = estimateProbability(level = 20, size = 60)

        assertTrue("Expected higher shape probability on larger boards", large > small)
    }

    private fun estimateProbability(level: Int, size: Int, trials: Int = 5000): Float {
        val random = Random(12345)
        val config = LevelConfiguration(size, size, maxSnakeLength = 8, maxLives = 3)
        var hits = 0
        repeat(trials) {
            if (ShapeProgressionPolicy.shouldApplyShape(config, level, random)) hits++
        }
        return hits.toFloat() / trials
    }
}
