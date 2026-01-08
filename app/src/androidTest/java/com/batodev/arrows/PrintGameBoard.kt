package com.batodev.arrows

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameGenerator
import com.batodev.arrows.engine.Point

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PrintGameBoard {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.batodev.arrows", appContext.packageName)
        Log.d("TAG", "test")
        println("Works!")
    }

    @Test
    fun testPrintGameBoard() {
        val engine = GameGenerator()
        val level = engine.generateSolvableLevel(width = 7, height = 10, 10)

        println("\n" + "=".repeat(50))
        println("Generated Arrows Puzzle - ASCII Art")
        println("=".repeat(50))
        println("Board Size: ${level.width} x ${level.height}")
        println("Number of Snakes: ${level.snakes.size}")
        println("=".repeat(50) + "\n")

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

        println("\n" + "=".repeat(50))
        println("Legend:")
        println("  .  = Empty cell")
        println(" ↑↓←→ = Snake head with direction")
        println("  ##  = Snake body segment (number = snake ID)")
        println("=".repeat(50) + "\n")

        // Print snake details
        println("Snake Details:")
        for (snake in level.snakes) {
            println("Snake ${snake.id}: Length = ${snake.body.size}, Head at ${snake.body.first()}, Direction = ${snake.headDirection}")
        }
        println()
    }
}
