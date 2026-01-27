package com.batodev.arrows.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val TEST_WIDTH = 20
private const val TEST_HEIGHT = 20
private const val TEST_SNAKE_LEN = 7
private const val SIMULATIONS_OCCUPIED = 10
private const val SIMULATIONS_SOLVABLE = 50

class GameGeneratorTest {

    @get:Rule
    val timeout: Timeout = Timeout(180, TimeUnit.SECONDS)

    @Test
    fun testEverySpotOccupiedAfterGenerationParallel() = runBlocking {
        val generator = GameGenerator()
        val failures = AtomicInteger(0)
        val dispatcher = Dispatchers.Default.limitedParallelism(
            Runtime.getRuntime().availableProcessors()
        )

        (1..SIMULATIONS_OCCUPIED).map { _ ->
            async(dispatcher) {
                val params = GenerationParams(TEST_WIDTH, TEST_HEIGHT, TEST_SNAKE_LEN, true)
                val level = generator.generateSolvableLevel(params)
                val occupiedCount = level.snakes.sumOf { it.body.size }

                if (occupiedCount != TEST_WIDTH * TEST_HEIGHT) {
                    failures.incrementAndGet()
                }
            }
        }.awaitAll()

        assertEquals("Some simulations failed: Not all spots occupied", 0, failures.get())
    }

    @Test
    fun testEveryPuzzleIsSolvable() = runBlocking {
        val generator = GameGenerator()
        val failures = AtomicInteger(0)
        val dispatcher = Dispatchers.Default.limitedParallelism(
            Runtime.getRuntime().availableProcessors()
        )

        (1..SIMULATIONS_SOLVABLE).map { _ ->
            async(dispatcher) {
                val params = GenerationParams(TEST_WIDTH, TEST_HEIGHT, TEST_SNAKE_LEN)
                val level = generator.generateSolvableLevel(params)
                if (!SolvabilityChecker.isResolvable(level)) {
                    failures.incrementAndGet()
                }
            }
        }.awaitAll()

        assertEquals("Some puzzles were not solvable", 0, failures.get())
    }
}
