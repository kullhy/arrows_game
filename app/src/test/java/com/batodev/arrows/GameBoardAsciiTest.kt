package com.batodev.arrows

import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameGenerator
import com.batodev.arrows.engine.Point
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

/**
 * Unit test to generate and print game board as ASCII art
 */
@RunWith(RobolectricTestRunner::class)

class GameBoardAsciiTest {

    @get:Rule
    val timeout: Timeout = Timeout(30, TimeUnit.SECONDS)

    @Test
    fun printGameBoardAsAsciiArt() {
        val engine = GameGenerator()
        val level = engine.generateSolvableLevel(width = 39, height = 39, maxSnakeLength = 18) { progress ->
            println("Progress: $progress")
        }

        println("\n" + "=".repeat(60))
        println("Generated Arrows Puzzle - ASCII Art")
        println("=".repeat(60))
        println("Board Size: ${level.width} x ${level.height}")
        println("Number of Snakes: ${level.snakes.size}")
        println("=".repeat(60) + "\n")

        // Create a 2D grid to map positions to snakes
        val grid = Array(level.width) { IntArray(level.height) { 0 } }
        val snakeMap = mutableMapOf<Int, com.batodev.arrows.engine.Snake>()

        // Fill the grid with snake IDs
        for (snake in level.snakes) {
            snakeMap[snake.id] = snake
            for (point in snake.body) {
                grid[point.x][point.y] = snake.id
            }
        }

        // Print the board
        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                val snakeId = grid[x][y]
                if (snakeId == 0) {
                    print(" . ")  // Empty cell
                } else {
                    val snake = snakeMap[snakeId]!!
                    val point = Point(x, y)

                    // Head is the first element of body
                    if (snake.body.first() == point) {
                        // Print arrow based on direction
                        val arrow = when (snake.headDirection) {
                            Direction.UP -> " ↑ "
                            Direction.DOWN -> " ↓ "
                            Direction.LEFT -> " ← "
                            Direction.RIGHT -> " → "
                        }
                        print(arrow)
                    } else {
                        // Print snake ID for body segments
                        print(String.format("%2d ", snakeId % 100))
                    }
                }
            }
            println()
        }

        println("\n" + "=".repeat(60))
        println("Legend:")
        println("  .   = Empty cell")
        println(" ↑↓←→ = Snake head with direction arrow")
        println("  ##  = Snake body segment (number = snake ID)")
        println("=".repeat(60) + "\n")

        // Print snake details
        println("Snake Details:")
        for (snake in level.snakes) {
            println("Snake ${snake.id}: Length = ${snake.body.size}, Head at ${snake.body.first()}, Direction = ${snake.headDirection}")
        }
        println()
    }

    @Test
    fun printMultipleBoards() {
        println("\n" + "█".repeat(60))
        println("Generating 3 Different Puzzle Boards")
        println("█".repeat(60))

        for (i in 1..3) {
            val engine = GameGenerator()
            val level = engine.generateSolvableLevel(width = 6, height = 8, maxSnakeLength = 4)

            println("\n" + "─".repeat(60))
            println("Board #$i - ${level.width}x${level.height} with ${level.snakes.size} snakes")
            println("─".repeat(60))

            val grid = Array(level.width) { IntArray(level.height) { 0 } }
            val snakeMap = mutableMapOf<Int, com.batodev.arrows.engine.Snake>()

            for (snake in level.snakes) {
                snakeMap[snake.id] = snake
                for (point in snake.body) {
                    grid[point.x][point.y] = snake.id
                }
            }

            for (y in 0 until level.height) {
                for (x in 0 until level.width) {
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

        println("\n" + "█".repeat(60) + "\n")
    }
}
