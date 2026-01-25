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

const val DEFAULT_TOLERANCE = 1.3f
const val INITIAL_LIVES = 5
private const val FLASH_DURATION_MS = 500L
private const val REMOVAL_FRAME_DELAY_MS = 16L

private const val BASE_BOARD_SIZE = 5
private const val SIZE_REDUCTION_PER_STEP = 3
private const val LIVES_REDUCTION_PER_STEP = 1
private const val LEVELS_PER_PROGRESSION_STEP = 10

class GameEngine(
    private val coroutineScope: CoroutineScope,
    private val repository: UserPreferencesRepository,
    private val gameGenerator: GameGenerator = GameGenerator(),
    autoLoad: Boolean = true,
    private val backgroundDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
    private val onVibrate: () -> Unit = {},
    private val soundManager: com.batodev.arrows.SoundManager? = null
) {
    private val gson = Gson()
    private var initialLevel: GameLevel? = null
    private var isVibrationEnabled = true
    private var isSoundsEnabled = true
    private var isFillBoardEnabled by mutableStateOf(false)
    private var animationSpeed = "Medium"

    var levelNumber by mutableIntStateOf(1)
        private set

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

    var lives by mutableIntStateOf(INITIAL_LIVES)
        private set

    var maxLives by mutableIntStateOf(INITIAL_LIVES)
        private set

    var scale by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)

    var flashingSnakeId by mutableStateOf<Int?>(null)
        private set

    var removalProgress by mutableStateOf<Map<Int, Float>>(emptyMap())
        private set

    init {
        observePreferences()
        if (autoLoad) {
            loadOrRegenerateLevel()
        }
    }

    private fun observePreferences() {
        coroutineScope.launch {
            repository.isVibrationEnabled.collect { isVibrationEnabled = it }
        }
        coroutineScope.launch {
            repository.isFillBoardEnabled.collect { isFillBoardEnabled = it }
        }
        coroutineScope.launch {
            repository.levelNumber.collect { levelNumber = it }
        }
        coroutineScope.launch {
            repository.isSoundsEnabled.collect {
                isSoundsEnabled = it
                soundManager?.setSoundsEnabled(it)
            }
        }
        coroutineScope.launch {
            repository.animationSpeed.collect { animationSpeed = it }
        }
    }

    fun loadOrRegenerateLevel() {
        coroutineScope.launch(backgroundDispatcher) {
            val savedInitial = repository.initialLevel.firstOrNull()
            val savedCurrent = repository.currentLevel.firstOrNull()
            val savedLives = repository.currentLives.firstOrNull()

            if (savedInitial != null && savedCurrent != null) {
                try {
                    val currentLevelNum = repository.levelNumber.firstOrNull() ?: 1
                    val config = calculateLevelConfiguration(currentLevelNum)
                    
                    initialLevel = gson.fromJson(savedInitial, GameLevel::class.java)
                    level = gson.fromJson(savedCurrent, GameLevel::class.java)
                    totalSnakesInLevel = initialLevel?.snakes?.size ?: 0
                    isGameWon = level.snakes.isEmpty()
                    maxLives = config.maxLives
                    lives = savedLives ?: config.maxLives
                    isLoading = false
                } catch (_: Exception) {
                    regenerateLevel()
                }
            } else {
                regenerateLevel()
            }
        }
    }

    private fun saveState() {
        coroutineScope.launch(backgroundDispatcher) {
            repository.saveCurrentLevel(gson.toJson(level))
            repository.saveCurrentLives(lives)
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
            saveState()
        }
    }

    fun onTap(
        tapOffset: androidx.compose.ui.geometry.Offset,
        containerWidthPx: Float,
        containerHeightPx: Float
    ) {
        if (isLoading || lives <= 0) return

        val gridCoords = transformTapToGrid(tapOffset, containerWidthPx, containerHeightPx)
        val tappedSnake = findTappedSnake(gridCoords.x, gridCoords.y)

        if (tappedSnake != null) {
            handleSnakeTap(tappedSnake)
        }
    }

    private fun transformTapToGrid(
        tapOffset: androidx.compose.ui.geometry.Offset,
        containerWidth: Float,
        containerHeight: Float
    ): androidx.compose.ui.geometry.Offset {
        // Inverse graphicsLayer transformation
        val centerX = containerWidth / 2
        val centerY = containerHeight / 2
        val transformedX = (tapOffset.x - offsetX - centerX) / scale + centerX
        val transformedY = (tapOffset.y - offsetY - centerY) / scale + centerY

        // Centered board bounds
        val cellSize = kotlin.math.min(containerWidth / level.width, containerHeight / level.height)
        val boardWidth = cellSize * level.width
        val boardHeight = cellSize * level.height
        val leftOffset = (containerWidth - boardWidth) / 2
        val topOffset = (containerHeight - boardHeight) / 2

        // Grid coordinates
        val cellX = (transformedX - leftOffset) / cellSize
        val cellY = (transformedY - topOffset) / cellSize

        return androidx.compose.ui.geometry.Offset(cellX, cellY)
    }

    private fun findTappedSnake(cellX: Float, cellY: Float): Snake? {
        return level.snakes
            .map { snake ->
                val head = snake.body.first()
                val tapAreaCenterX = head.x + 0.5f + snake.headDirection.dx * TAP_AREA_OFFSET_FACTOR
                val tapAreaCenterY = head.y + 0.5f + snake.headDirection.dy * TAP_AREA_OFFSET_FACTOR

                val dx = tapAreaCenterX - cellX
                val dy = tapAreaCenterY - cellY
                val distSq = dx * dx + dy * dy

                Triple(snake, distSq, isLineOfSightObstructed(snake))
            }
            .filter { it.second <= DEFAULT_TOLERANCE * DEFAULT_TOLERANCE }
            .minWithOrNull(compareBy({ it.third }, { it.second }))
            ?.first
    }

    private fun handleSnakeTap(snake: Snake) {
        if (isVibrationEnabled) {
            onVibrate()
        }

        if (isLineOfSightObstructed(snake)) {
            handleObstructedTap(snake)
        } else {
            handleSuccessfulTap(snake)
        }
    }

    private fun handleObstructedTap(snake: Snake) {
        if (lives > 0) {
            lives--
            saveState()
            if (lives > 0) {
                soundManager?.playLiveLost()
            } else {
                soundManager?.playGameLost()
            }
        }

        flashingSnakeId = snake.id
        coroutineScope.launch {
            delay(FLASH_DURATION_MS)
            flashingSnakeId = null
        }
    }

    private fun handleSuccessfulTap(snake: Snake) {
        soundManager?.playRandomSwitch()
        if (!removalProgress.containsKey(snake.id)) {
            animateRemoval(snake.id)
        }
    }

    fun regenerateLevel() {
        isLoading = true
        loadingProgress = 0f
        coroutineScope.launch(backgroundDispatcher) {
            val fillBoard = repository.isFillBoardEnabled.firstOrNull() ?: false
            val currentLevelNum = repository.levelNumber.firstOrNull() ?: 1

            val config = calculateLevelConfiguration(currentLevelNum)

            val newLevel = gameGenerator.generateSolvableLevel(
                config.width, config.height, config.maxSnakeLength,
                onProgress = { progress -> loadingProgress = progress },
                fillTheBoard = fillBoard
            )

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                applyNewLevel(newLevel, config)
            }
        }
    }

    private fun calculateLevelConfiguration(levelNum: Int): LevelConfiguration {
        val progressionStep = levelNum / LEVELS_PER_PROGRESSION_STEP
        val sizeReduction = progressionStep * SIZE_REDUCTION_PER_STEP
        val livesReduction = progressionStep * LIVES_REDUCTION_PER_STEP

        // Base progression (growing) minus penalty reduction
        // L1: 5x5, L2: 5x6, L3: 6x6, L4: 6x7, L5: 7x7...
        val baseH = BASE_BOARD_SIZE + (levelNum - 1) / 2
        val baseW = BASE_BOARD_SIZE + levelNum / 2
        
        val h = (baseH - sizeReduction).coerceAtLeast(1)
        val w = (baseW - sizeReduction).coerceAtLeast(1)
        
        val maxLives = (INITIAL_LIVES - livesReduction).coerceAtLeast(1)
        val maxSnakeLength = (3 + levelNum / 2).coerceIn(4, 30)
        
        return LevelConfiguration(w, h, maxSnakeLength, maxLives)
    }

    private data class LevelConfiguration(val width: Int, val height: Int, val maxSnakeLength: Int, val maxLives: Int)

    private fun applyNewLevel(newLevel: GameLevel, config: LevelConfiguration) {
        initialLevel = newLevel
        level = newLevel
        totalSnakesInLevel = newLevel.snakes.size
        isGameWon = false
        maxLives = config.maxLives
        lives = config.maxLives
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        flashingSnakeId = null
        removalProgress = emptyMap()
        isLoading = false

        coroutineScope.launch(backgroundDispatcher) {
            val json = gson.toJson(newLevel)
            repository.saveInitialLevel(json)
            repository.saveCurrentLevel(json)
            repository.saveCurrentLives(lives)
        }
    }

    private fun animateRemoval(snakeId: Int) {
        val durationMs = when (animationSpeed) {
            "High" -> 300L
            "Low" -> 900L
            else -> 600L
        }
        coroutineScope.launch {
            removalProgress = removalProgress.toMutableMap().apply { put(snakeId, 0f) }
            var elapsed = 0L
            while (elapsed < durationMs && isActive) {
                delay(REMOVAL_FRAME_DELAY_MS)
                elapsed += REMOVAL_FRAME_DELAY_MS
                val linearP = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                // Apply ease-in-cubic for acceleration (more progress per frame as time goes on)
                val p = linearP * linearP * linearP
                removalProgress = removalProgress.toMutableMap().apply { put(snakeId, p) }
            }
            removalProgress = removalProgress.toMutableMap().apply { put(snakeId, 1f) }
            level = level.copy(snakes = level.snakes.filter { it.id != snakeId })
            removalProgress = removalProgress.toMutableMap().apply { remove(snakeId) }

            if (level.snakes.isEmpty()) {
                handleGameWon()
            } else {
                soundManager?.playSnakeRemoved()
                saveState()
            }
        }
    }

    private fun handleGameWon() {
        isGameWon = true
        soundManager?.playGameWon()
        coroutineScope.launch(backgroundDispatcher) {
            val nextLevel = levelNumber + 1
            repository.saveLevelNumber(nextLevel)
            repository.clearSavedLevel()
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
