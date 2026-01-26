package com.batodev.arrows.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)

class GameGeneratorShapeTest {

    @Test
    fun testGenerateLevelWithHeartShapeAndPrintAscii() {
        val context = RuntimeEnvironment.getApplication()

        // Find the heart drawable in the project resources
        // Since we are running in unit test, Robolectric needs to find the resources.
        // heart.png is in app/src/main/res/drawable/heart.png
        val resId = context.resources.getIdentifier("heart", "drawable", context.packageName)
        assertTrue("Heart drawable not found", resId != 0)

        val options = BitmapFactory.Options().apply {
            inScaled = false
        }
        val heartBitmap = BitmapFactory.decodeResource(context.resources, resId, options)
        assertTrue("Failed to decode heart bitmap", heartBitmap != null)

        val generator = GameGenerator()
        val width = 20
        val height = 20

        // Generate level with the shape
        val level = generator.generateSolvableLevel(
            width = width,
            height = height,
            maxSnakeLength = 10,
            fillTheBoard = true,
            shapeBitmap = heartBitmap
        )

        // ASCII Printing Logic
        println("\n" + "=".repeat(60))
        println("Heart Shape Puzzle - ASCII Art")
        println("=".repeat(60) + "\n")
        println("Board Size: ${level.width} x ${level.height}")
        println("Number of Snakes: ${level.snakes.size}")
        println("=".repeat(60) + "\n")

        val grid = Array(level.width) { IntArray(level.height) { 0 } }
        val snakeMap = mutableMapOf<Int, Snake>()

        for (snake in level.snakes) {
            snakeMap[snake.id] = snake
            for (point in snake.body) {
                grid[point.x][point.y] = snake.id
            }
        }

        // Re-calculate walls for printing and validation
        val clipped = clipToBlackContent(heartBitmap)
        val scaled = Bitmap.createScaledBitmap(clipped, width, height, false)

        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                val snakeId = grid[x][y]
                val pixel = scaled.getPixel(x, y)
                val isWallPixel = !(Color.alpha(pixel) > 128 &&
                        Color.red(pixel) < 128 &&
                        Color.green(pixel) < 128 &&
                        Color.blue(pixel) < 128)

                if (isWallPixel) {
                    print(" # ") // Wall/Forbidden
                } else if (snakeId == 0) {
                    print(" . ") // Valid but empty
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

        println("\n" + "=".repeat(60))
        println("Legend:")
        println("  #   = Forbidden area (Wall)")
        println("  .   = Valid playable empty cell")
        println(" ↑↓←→ = Snake head")
        println("  ##  = Snake body segment")
        println("=".repeat(60) + "\n")

        // Validation
        level.snakes.forEach { snake ->
            snake.body.forEach { point ->
                val pixel = scaled.getPixel(point.x, point.y)
                val isBlack = Color.alpha(pixel) > 128 &&
                        Color.red(pixel) < 128 &&
                        Color.green(pixel) < 128 &&
                        Color.blue(pixel) < 128

                assertTrue(
                    "Snake ${snake.id} body at (${point.x}, ${point.y}) is on a wall!",
                    isBlack
                )
            }
        }
    }

    private fun clipToBlackContent(bitmap: Bitmap): Bitmap {
        var minX = bitmap.width
        var minY = bitmap.height
        var maxX = 0
        var maxY = 0
        var found = false

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                if (Color.alpha(pixel) > 128 &&
                    Color.red(pixel) < 128 &&
                    Color.green(pixel) < 128 &&
                    Color.blue(pixel) < 128
                ) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                    found = true
                }
            }
        }

        if (!found) return bitmap

        val width = maxX - minX + 1
        val height = maxY - minY + 1
        return Bitmap.createBitmap(bitmap, minX, minY, width, height)
    }
}
