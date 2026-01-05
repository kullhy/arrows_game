package com.batodev.arrows

import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.engine.Point
import org.junit.Test

/**
 * Unit test to generate and print game board as ASCII art
 */
class GameBoardAsciiTest {

    @Test
    fun printGameBoardAsAsciiArt() {
        val engine = GameEngine()
        val level = engine.generateSolvableLevel(width = 7, height = 10, fillDensity = 0.95)

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

                    // Check if this is the head (last element in body)
                    if (snake.body.last() == point) {
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
            println("Snake ${snake.id}: Length = ${snake.body.size}, Head at ${snake.body.last()}, Direction = ${snake.headDirection}")
        }
        println()
    }

    @Test
    fun printMultipleBoards() {
        println("\n" + "█".repeat(60))
        println("Generating 3 Different Puzzle Boards")
        println("█".repeat(60))

        for (i in 1..3) {
            val engine = GameEngine()
            val level = engine.generateSolvableLevel(width = 7, height = 10, fillDensity = 0.95)

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

                        if (snake.body.last() == point) {
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

