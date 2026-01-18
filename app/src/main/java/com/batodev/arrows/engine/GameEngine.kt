package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.batodev.arrows.TAP_AREA_OFFSET_FACTOR
import com.batodev.arrows.data.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameEngine(
    private val coroutineScope: CoroutineScope,
    private val repository: UserPreferencesRepository,
    private val gameGenerator: GameGenerator = GameGenerator(),
    autoLoad: Boolean = true,
    private val backgroundDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
    private val onVibrate: () -> Unit = {}
) {
    private val gson = Gson()
    private var initialLevel: GameLevel? = null
    private var isVibrationEnabled = true

    var level by mutableStateOf(GameLevel(1, 1, emptyList()))
        private set

    var isLoading by mutableStateOf(true)
        private set

    var totalSnakesInLevel by mutableIntStateOf(0)
        private set

    var isGameWon by mutableStateOf(false)
        private set

    var loadingProgress by mutableFloatStateOf(0f)
        private set

    var lives by mutableIntStateOf(3)
        private set

    private val maxLives = 3

    var scale by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)

    var flashingSnakeId by mutableStateOf<Int?>(null)
        private set

    var removalProgress by mutableStateOf<Map<Int, Float>>(emptyMap())
        private set

    init {
        coroutineScope.launch {
            repository.isVibrationEnabled.collect {
                isVibrationEnabled = it
            }
        }
        if (autoLoad) {
            loadOrRegenerateLevel()
        }
    }

    fun loadOrRegenerateLevel() {
        coroutineScope.launch(backgroundDispatcher) {
            val savedInitial = repository.initialLevel.firstOrNull()
            val savedCurrent = repository.currentLevel.firstOrNull()

            if (savedInitial != null && savedCurrent != null) {
                try {
                    initialLevel = gson.fromJson(savedInitial, GameLevel::class.java)
                    level = gson.fromJson(savedCurrent, GameLevel::class.java)
                    totalSnakesInLevel = initialLevel?.snakes?.size ?: 0
                    isGameWon = level.snakes.isEmpty()
                    lives = maxLives
                    isLoading = false
                } catch (_: Exception) {
                    regenerateLevel()
                }
            } else {
                regenerateLevel()
                isLoading = false
            }
        }
    }

    private fun saveState() {
        coroutineScope.launch(backgroundDispatcher) {
            repository.saveCurrentLevel(gson.toJson(level))
        }
    }

    fun restartLevel() {
        initialLevel?.let {
            level = it
            totalSnakesInLevel = it.snakes.size
            isGameWon = false
            lives = maxLives
            scale = 1f
            offsetX = 0f
            offsetY = 0f
            flashingSnakeId = null
            removalProgress = emptyMap()
            saveState()
        }
    }

    fun onTransform(pan: androidx.compose.ui.geometry.Offset, zoom: Float) {
        scale = (scale * zoom).coerceIn(0.2f, 6f)
        offsetX += pan.x
        offsetY += pan.y
    }

    fun addLife() {
        if (lives < maxLives) {
            lives++
        }
    }

    fun onTap(
        tapOffset: androidx.compose.ui.geometry.Offset,
        boardSizePx: Float
    ) {
        if (isLoading || lives <= 0) return

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
            if (isVibrationEnabled) {
                onVibrate()
            }
            if (isLineOfSightObstructed(tappedSnake)) {
                // Deduct life on obstructed move
                if (lives > 0) {
                    lives--
                }

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
        coroutineScope.launch(backgroundDispatcher) {
            val newLevel = gameGenerator.generateSolvableLevel(25, 25, 30, onProgress = { progress ->
                loadingProgress = progress
            }, fillTheBoard = true)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                initialLevel = newLevel
                level = newLevel
                totalSnakesInLevel = newLevel.snakes.size
                isGameWon = false
                lives = maxLives
                scale = 1f
                offsetX = 0f
                offsetY = 0f
                flashingSnakeId = null
                removalProgress = emptyMap()
                isLoading = false

                // Save both initial and current as same for a new level
                coroutineScope.launch(backgroundDispatcher) {
                    val json = gson.toJson(newLevel)
                    repository.saveInitialLevel(json)
                    repository.saveCurrentLevel(json)
                }
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
                coroutineScope.launch(backgroundDispatcher) {
                    repository.clearSavedLevel()
                }
            } else {
                saveState()
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
