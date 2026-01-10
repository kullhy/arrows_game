package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameEngine(
    private val coroutineScope: CoroutineScope,
    private val gameGenerator: GameGenerator = GameGenerator()
) {
    var level by mutableStateOf(gameGenerator.generateSolvableLevel(15, 15, 12))
        private set

    var scale by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)

    var flashingSnakeId by mutableStateOf<Int?>(null)
        private set

    fun onTransform(pan: androidx.compose.ui.geometry.Offset, zoom: Float) {
        scale = (scale * zoom).coerceIn(0.3f, 5f)
        offsetX += pan.x
        offsetY += pan.y
    }

    fun onTap(
        tapOffset: androidx.compose.ui.geometry.Offset,
        boardSizePx: Float
    ) {
        // Transform tap coordinates to content coordinates
        val center = boardSizePx / 2
        val contentX = (tapOffset.x - offsetX - center) / scale + center
        val contentY = (tapOffset.y - offsetY - center) / scale + center

        // Convert to grid cell coordinates
        val cellWidth = boardSizePx / level.width
        val cellHeight = boardSizePx / level.height
        val cellX = (contentX / cellWidth).toInt()
        val cellY = (contentY / cellHeight).toInt()

        // Check if tapped cell contains a snake head
        val tappedSnake = level.snakes.find { snake ->
            val head = snake.body.first()
            head.x == cellX && head.y == cellY
        }

        if (tappedSnake != null) {
            if (isLineOfSightObstructed(tappedSnake)) {
                // Flash red
                flashingSnakeId = tappedSnake.id
                coroutineScope.launch {
                    delay(500) // Flash duration
                    flashingSnakeId = null
                }
            } else {
                // Remove snake from board
                level = level.copy(
                    snakes = level.snakes.filter { it.id != tappedSnake.id }
                )
            }
        }
    }

    fun regenerateLevel() {
        level = gameGenerator.generateSolvableLevel(15, 15, 12)
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        flashingSnakeId = null
    }

    private fun isLineOfSightObstructed(snake: Snake): Boolean {
        val head = snake.body.first()
        val direction = snake.headDirection
        var current = Point(head.x + direction.dx, head.y + direction.dy)

        while (current.x in 0 until level.width && current.y in 0 until level.height) {
            // Check if any snake occupies this position
            val occupied = level.snakes.any { otherSnake ->
                otherSnake.body.any { segment -> segment == current }
            }
            if (occupied) {
                return true
            }
            current = Point(current.x + direction.dx, current.y + direction.dy)
        }
        return false
    }
}
