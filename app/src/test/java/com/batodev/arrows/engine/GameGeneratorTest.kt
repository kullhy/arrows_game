package com.batodev.arrows.engine

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)

class GameGeneratorTest {

    @get:Rule
    val timeout: Timeout = Timeout(90, TimeUnit.SECONDS)

    val width = 40
    val height = 40

    @Test
    fun testEverySpotOccupiedAfterGenerationParallel() = runBlocking {
        val engine = GameGenerator()
        val simulations = 10
        val failures = AtomicInteger(0)

        val dispatcher = Dispatchers.Default.limitedParallelism(Runtime.getRuntime().availableProcessors())

        (1..simulations).map { sim ->
            async(dispatcher) {
                val level = engine.generateSolvableLevel(width, height, 7, true)
                var occupiedCount = 0
                val grid = Array(width) { IntArray(height) { 0 } }
                for (snake in level.snakes) {
                    for (p in snake.body) {
                        grid[p.x][p.y] = snake.id
                        occupiedCount++
                    }
                }

                if (occupiedCount != width * height) {
                    failures.incrementAndGet()
                    println("Simulation $sim/$simulations: FAILED - Not all spots occupied. Occupied: $occupiedCount, Total: ${width * height}")
                }
            }
        }.awaitAll()

        println("Completed $simulations parallel simulations. Total failures: ${failures.get()}")
        assertEquals("Some simulations failed: Not all spots occupied", 0, failures.get())
    }

    /**
     * How it tests:
     * 1. Check every snake.
     * 2. At least one snakes head must point to the edge of the board
     * with an un obstructed line of sight. No other snake or self in
     * the straight line to the edge.
     * 3. Remove that snake from the board.
     * 4. Repeat point 1 until no snakes are on the board.
     */
    @Test
    fun testEveryPuzzleIsSolvable() = runBlocking {
        val engine = GameGenerator()
        val simulations = 50
        val failures = AtomicInteger(0)

        val dispatcher = Dispatchers.Default.limitedParallelism(Runtime.getRuntime().availableProcessors())

        (1..simulations).map { sim ->
            async(dispatcher) {
                val level = engine.generateSolvableLevel(width, height, 7)

                // Create a grid to track which cells are occupied
                val grid = Array(width) { IntArray(height) { 0 } }
                val snakeMap = mutableMapOf<Int, Snake>()

                // Fill the grid with snake IDs
                for (snake in level.snakes) {
                    snakeMap[snake.id] = snake
                    for (p in snake.body) {
                        grid[p.x][p.y] = snake.id
                    }
                }

                val remainingSnakes = snakeMap.keys.toMutableSet()
                var iterationCount = 0
                val maxIterations = level.snakes.size + 10 // Safety limit

                while (remainingSnakes.isNotEmpty()) {
                    iterationCount++

                    if (iterationCount > maxIterations) {
                        failures.incrementAndGet()
                        println("Simulation $sim/$simulations: FAILED - Infinite loop detected. Remaining snakes: ${remainingSnakes.size}")
                        break
                    }

                    // Find a snake that can be removed (has clear line of sight to edge)
                    val removableSnake = findRemovableSnake(remainingSnakes, snakeMap, grid)

                    if (removableSnake == null) {
                        failures.incrementAndGet()
                        println("Simulation $sim/$simulations: FAILED - No removable snake found. Remaining snakes: ${remainingSnakes.size}")
                        printBoard(grid, snakeMap)
                        break
                    }

                    // Remove the snake from the grid
                    val snake = snakeMap[removableSnake]!!
                    for (p in snake.body) {
                        grid[p.x][p.y] = 0
                    }
                    remainingSnakes.remove(removableSnake)
                }

                if (remainingSnakes.isEmpty()) {
                    println("Simulation $sim/$simulations: SUCCESS - All snakes removed in $iterationCount steps")
                }
            }
        }.awaitAll()

        println("Completed $simulations parallel simulations. Total failures: ${failures.get()}")
        assertEquals("Some puzzles were not solvable", 0, failures.get())
    }

    /**
     * Finds a snake that has an unobstructed line of sight to the edge of the board.
     * Returns the snake ID or null if no such snake exists.
     */
    private fun findRemovableSnake(
        remainingSnakes: Set<Int>,
        snakeMap: Map<Int, Snake>,
        grid: Array<IntArray>
    ): Int? {
        for (snakeId in remainingSnakes) {
            val snake = snakeMap[snakeId]!!
            val head = snake.body.first()
            val direction = snake.headDirection

            // Check if the line of sight to the edge is clear
            if (hasCleanLineOfSight(head, direction, snake.id, grid)) {
                return snakeId
            }
        }
        return null
    }

    /**
     * Checks if a snake head has a clear line of sight to the edge of the board.
     * The path must not contain any other snake parts (excluding its own body segments in the path).
     */
    private fun hasCleanLineOfSight(
        head: Point,
        direction: Direction,
        snakeId: Int,
        grid: Array<IntArray>
    ): Boolean {
        var current = head + direction

        while (current.x in 0 until width && current.y in 0 until height) {
            val cellSnakeId = grid[current.x][current.y]

            // If there's another snake (not this one) blocking the path
            if (cellSnakeId != 0 && cellSnakeId != snakeId) {
                return false
            }

            current += direction
        }

        // We've reached the edge without obstruction
        return true
    }

    /**
     * Helper function to print the board state for debugging.
     */
    private fun printBoard(grid: Array<IntArray>, snakeMap: Map<Int, Snake>) {
        println("Current board state:")
        for (y in 0 until height) {
            for (x in 0 until width) {
                val snakeId = grid[x][y]
                if (snakeId == 0) {
                    print(" . ")
                } else {
                    val snake = snakeMap[snakeId]!!
                    val point = Point(x, y)
                    if (snake.body.first() == point) {
                        val arrow = when (snake.headDirection) {
                            Direction.UP -> " ↑ "
                            Direction.DOWN -> " ↓ "
                            Direction.LEFT -> " ← "
                            Direction.RIGHT -> " → "
                        }
                        print(arrow)
                    } else {
                        print(String.format("%2d ", snakeId % 100))
                    }
                }
            }
            println()
        }
    }
}
