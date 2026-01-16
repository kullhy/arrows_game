package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.batodev.arrows.TAP_AREA_OFFSET_FACTOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameEngine(
    private val coroutineScope: CoroutineScope,
    private val gameGenerator: GameGenerator = GameGenerator()
) {
    var level by mutableStateOf(GameLevel(1, 1, emptyList()))
        private set

    var isLoading by mutableStateOf(true)
        private set

    var totalSnakesInLevel by mutableStateOf(0)
        private set

    var isGameWon by mutableStateOf(false)
        private set

    var loadingProgress by mutableFloatStateOf(0f)
        private set

    var scale by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)

    var flashingSnakeId by mutableStateOf<Int?>(null)
        private set

    var removalProgress by mutableStateOf<Map<Int, Float>>(emptyMap())
        private set

    init {
        regenerateLevel()
    }

    fun onTransform(pan: androidx.compose.ui.geometry.Offset, zoom: Float) {
        scale = (scale * zoom).coerceIn(0.3f, 5f)
        offsetX += pan.x
        offsetY += pan.y
    }

    fun onTap(
        tapOffset: androidx.compose.ui.geometry.Offset,
        boardSizePx: Float
    ) {
        if (isLoading) return

        // Transform tap coordinates to content coordinates
        val center = boardSizePx / 2
        val contentX = (tapOffset.x - offsetX - center) / scale + center
        val contentY = (tapOffset.y - offsetY - center) / scale + center

        // Convert to grid cell coordinates
        val cellWidth = boardSizePx / level.width
        val cellHeight = boardSizePx / level.height
        val cellX = contentX / cellWidth
        val cellY = contentY / cellHeight

        // Check if tapped cell contains a snake head (with tolerance for easier tapping)
        val tolerance = 0.6f // Allow tapping within 0.6 cells of the tap area center
        val tappedSnake = level.snakes.find { snake ->
            val head = snake.body.first()
            // Account for the offset of tap area in arrow direction
            val tapAreaCenterX = head.x + 0.5f + snake.headDirection.dx * TAP_AREA_OFFSET_FACTOR
            val tapAreaCenterY = head.y + 0.5f + snake.headDirection.dy * TAP_AREA_OFFSET_FACTOR
            kotlin.math.abs(tapAreaCenterX - cellX) <= tolerance &&
            kotlin.math.abs(tapAreaCenterY - cellY) <= tolerance
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
                // Animate out, then remove
                if (!removalProgress.containsKey(tappedSnake.id)) {
                    animateRemoval(tappedSnake.id)
                }
            }
        }
    }

    fun regenerateLevel() {
        isLoading = true
        loadingProgress = 0f
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val newLevel = gameGenerator.generateSolvableLevel(15, 15, 30, onProgress = { progress ->
                loadingProgress = progress
            })
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                level = newLevel
                totalSnakesInLevel = newLevel.snakes.size
                isGameWon = false
                scale = 1f
                offsetX = 0f
                offsetY = 0f
                flashingSnakeId = null
                removalProgress = emptyMap()
                isLoading = false
            }
        }
    }

    private fun animateRemoval(snakeId: Int) {
        val durationMs = 600L
        val frameDelayMs = 16L
        coroutineScope.launch {
            removalProgress = removalProgress.toMutableMap().apply { put(snakeId, 0f) }
            var elapsed = 0L
            while (elapsed < durationMs && isActive) {
                delay(frameDelayMs)
                elapsed += frameDelayMs
                val linearP = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                // Apply ease-in-cubic for acceleration (more progress per frame as time goes on)
                val p = linearP * linearP * linearP
                removalProgress = removalProgress.toMutableMap().apply { put(snakeId, p) }
            }
            removalProgress = removalProgress.toMutableMap().apply { put(snakeId, 1f) }
            level = level.copy(snakes = level.snakes.filter { it.id != snakeId })
            removalProgress = removalProgress.toMutableMap().apply { remove(snakeId) }
            if (level.snakes.isEmpty()) {
                isGameWon = true
            }
        }
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
