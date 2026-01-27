package com.batodev.arrows.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class GameGeneratorShapeTest {

    private val boardWidth = 20
    private val boardHeight = 20
    private val maxSnakeLen = 10

    @Test
    fun testGenerateLevelWithHeartShapeAndPrintAscii() {
        val context = RuntimeEnvironment.getApplication()
        val resId = context.resources.getIdentifier(
            "favorite_256dp_000000_fill1_wght400_grad0_opsz48",
            "drawable", context.packageName)
        assertTrue("Heart drawable not found", resId != 0)

        val options = BitmapFactory.Options().apply { inScaled = false }
        val heartBitmap = BitmapFactory.decodeResource(context.resources, resId, options)
        assertTrue("Failed to decode heart bitmap", heartBitmap != null)

        val generator = GameGenerator()
        val params = GenerationParams(boardWidth, boardHeight, maxSnakeLen, true, heartBitmap)
        val level = generator.generateSolvableLevel(params)

        val imageProcessor = BoardImageProcessor()
        val walls = imageProcessor.createWallsFromImage(heartBitmap!!, boardWidth, boardHeight)

        printLevelAscii(level, walls)
        validateSnakesOnNonWallCells(level, heartBitmap)
    }

    private fun printLevelAscii(level: GameLevel, walls: Array<BooleanArray>) {
        val grid = Array(level.width) { IntArray(level.height) }
        val snakeMap = level.snakes.associateBy { it.id }
        level.snakes.forEach { s -> s.body.forEach { p -> grid[p.x][p.y] = s.id } }

        println("\nBoard Size: ${level.width} x ${level.height}\nNumber of Snakes: ${level.snakes.size}\n")
        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                printCell(x, y, grid[x][y], walls[x][y], snakeMap)
            }
            println()
        }
    }

    private fun printCell(x: Int, y: Int, snakeId: Int, isWall: Boolean, snakeMap: Map<Int, Snake>) {
        if (isWall) {
            print(" # ")
        } else if (snakeId == 0) {
            print(" . ")
        } else {
            val snake = snakeMap[snakeId]!!
            val point = Point(x, y)
            if (snake.body.first() == point) {
                print(
                    when (snake.headDirection) {
                        Direction.UP -> " ↑ "
                        Direction.DOWN -> " ↓ "
                        Direction.LEFT -> " ← "
                        Direction.RIGHT -> " → "
                    }
                )
            } else {
                print(String.format(Locale.US, "%2d ", snakeId % 100))
            }
        }
    }

    private fun validateSnakesOnNonWallCells(level: GameLevel, heartBitmap: Bitmap) {
        val imageProcessor = BoardImageProcessor()
        val walls = imageProcessor.createWallsFromImage(heartBitmap, boardWidth, boardHeight)
        level.snakes.forEach { snake ->
            snake.body.forEach { point ->
                assertTrue(
                    "Snake ${snake.id} body at (${point.x}, ${point.y}) is on a wall!",
                    !walls[point.x][point.y]
                )
            }
        }
    }
}
