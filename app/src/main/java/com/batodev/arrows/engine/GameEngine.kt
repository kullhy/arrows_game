package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.batodev.arrows.GameConstants
import com.batodev.arrows.SoundManager
import com.batodev.arrows.data.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class GameEngineConfig(
    val coroutineScope: CoroutineScope,
    val repository: UserPreferencesRepository,
    val gameGenerator: GameGenerator = GameGenerator(),
    val autoLoad: Boolean = true,
    val isCustomGame: Boolean = false,
    val backgroundDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
)

data class GameEngineFeatures(
    val onVibrate: () -> Unit = {},
    val soundManager: SoundManager? = null,
    val shapeProvider: BoardShapeProvider? = null,
    val random: kotlin.random.Random = kotlin.random.Random.Default,
    val forcedWidth: Int? = null,
    val forcedHeight: Int? = null,
    val forcedShape: String? = null,
)

class GameEngine(config: GameEngineConfig, features: GameEngineFeatures = GameEngineFeatures()) {
    private val coroutineScope = config.coroutineScope
    private val repository = config.repository
    private val isCustomGame = config.isCustomGame
    private val backgroundDispatcher = config.backgroundDispatcher
    private val soundManager = features.soundManager
    private val gson = Gson()
    private val inputHandler = InputHandler()
    private val levelManager = LevelManager(
        repository, config.gameGenerator, features.shapeProvider, features.random, gson
    )
    internal val transformationState = TransformationState()
    private val removalAnimator = RemovalAnimator(coroutineScope)
    private val tapHandler = TapHandler(coroutineScope, soundManager, features.onVibrate)

    private var initialLevel: GameLevel? = null
    private var isVibrationEnabled = true
    private var animationSpeed = "Medium"
    private var forcedWidth: Int? = features.forcedWidth
    private var forcedHeight: Int? = features.forcedHeight
    private var forcedLives: Int? = null
    private var forcedShape: String? = features.forcedShape

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
    var lives by mutableIntStateOf(GameConstants.INITIAL_LIVES)
        private set
    var maxLives by mutableIntStateOf(GameConstants.INITIAL_LIVES)
        private set

    val scale get() = transformationState.scale
    val offsetX get() = transformationState.offsetX
    val offsetY get() = transformationState.offsetY
    val removalProgress get() = removalAnimator.removalProgress
    val flashingSnakeId get() = tapHandler.flashingSnakeId

    init {
        observePreferences()
        if (config.autoLoad) loadOrRegenerateLevel()
    }

    private fun observePreferences() {
        coroutineScope.launch { repository.isVibrationEnabled.collect { isVibrationEnabled = it } }
        coroutineScope.launch { repository.levelNumber.collect { levelNumber = it } }
        coroutineScope.launch {
            repository.isSoundsEnabled.collect {
                soundManager?.setSoundsEnabled(
                    it
                )
            }
        }
        coroutineScope.launch { repository.animationSpeed.collect { animationSpeed = it } }
        coroutineScope.launch {
            repository.debugForcedWidth.collect {
                if (forcedWidth == null) forcedWidth = it
            }
        }
        coroutineScope.launch {
            repository.debugForcedHeight.collect {
                if (forcedHeight == null) forcedHeight = it
            }
        }
        coroutineScope.launch { repository.debugForcedLives.collect { forcedLives = it } }
        coroutineScope.launch {
            repository.debugForcedShape.collect {
                if (forcedShape == null) forcedShape = it
            }
        }
    }

    fun loadOrRegenerateLevel() {
        coroutineScope.launch(backgroundDispatcher) {
            levelManager.loadLevel(
                onSuccess = { initial, current, maxL, currentL ->
                    initialLevel = initial; level = current; totalSnakesInLevel =
                    initial.snakes.size
                    isGameWon = level.snakes.isEmpty(); maxLives = maxL; lives =
                    currentL; isLoading = false
                },
                onFailure = { regenerateLevel() }
            )
        }
    }

    fun restartLevel() {
        initialLevel?.let {
            level = it; totalSnakesInLevel = it.snakes.size; isGameWon = false; lives = maxLives
            transformationState.reset(); tapHandler.clearFlash(); removalAnimator.clear(); saveState()
        }
    }

    fun showHint() {
        transformationState.reset()
        SolvabilityChecker.findRemovableSnake(level)?.let { removableId ->
            tapHandler.flashSnake(removableId)
        }
    }

    fun onTransform(pan: Offset, zoom: Float) = transformationState.transform(pan, zoom)

    fun addLife() {
        if (lives < maxLives) {
            lives++; saveState()
        }
    }

    fun onTap(tapOffset: Offset, containerWidthPx: Float, containerHeightPx: Float) {
        if (isLoading || lives <= 0) return
        val gridCoords = inputHandler.transformTapToGrid(
            TapTransformationParams(
                tapOffset,
                containerWidthPx,
                containerHeightPx,
                level,
                scale,
                offsetX,
                offsetY
            )
        )
        val tappedSnake = inputHandler.findTappedSnake(gridCoords.x, gridCoords.y, level.snakes) {
            SolvabilityChecker.isLineOfSightObstructed(
                level,
                it,
                removalAnimator.removalProgress.keys
            )
        }
        if (tappedSnake != null) {
            val isObstructed = SolvabilityChecker.isLineOfSightObstructed(
                level, tappedSnake, removalAnimator.removalProgress.keys
            )
            tapHandler.handleSnakeTap(
                TapParams(
                    tappedSnake, isVibrationEnabled, isObstructed, lives,
                    onPenalty = { lives--; saveState() },
                    onSuccess = {
                        if (!removalProgress.containsKey(tappedSnake.id)) {
                            removalAnimator.animate(
                                tappedSnake.id,
                                animationSpeed,
                                { _, _ -> },
                                { id ->
                                    onSnakeRemoved(id)
                                })
                        }
                    }
                )
            )
        }
    }

    private fun onSnakeRemoved(id: Int) {
        level = level.copy(snakes = level.snakes.filter { it.id != id })
        if (level.snakes.isEmpty()) {
            isGameWon = true
            soundManager?.playGameWon()
            // Clear saved game for both custom and regular games
            coroutineScope.launch(backgroundDispatcher) {
                if (isCustomGame) {
                    // For custom games, only clear the save without advancing level
                    levelManager.clearSavedGame()
                } else {
                    // For regular games, advance to next level and clear the save
                    levelManager.advanceLevel(levelNumber)
                }
            }
        } else {
            soundManager?.playSnakeRemoved()
            saveState()
        }
    }

    fun regenerateLevel() {
        isLoading = true; loadingProgress = 0f
        coroutineScope.launch(backgroundDispatcher) {
            levelManager.regenerateLevel(
                RegenerationParams(
                    forcedWidth, forcedHeight, forcedLives, forcedShape, isCustomGame,
                    onProgress = { loadingProgress = it },
                    onComplete = { newLevel, config ->
                        initialLevel = newLevel; level = newLevel; totalSnakesInLevel =
                        newLevel.snakes.size
                        isGameWon = false; maxLives = config.maxLives; lives = config.maxLives
                        transformationState.reset(); tapHandler.clearFlash(); removalAnimator.clear()
                        isLoading = false
                        coroutineScope.launch(backgroundDispatcher) {
                            levelManager.saveInitialState(newLevel, lives)
                        }
                    }
                )
            )
        }
    }

    private fun saveState() {
        coroutineScope.launch(backgroundDispatcher) { levelManager.saveState(level, lives) }
    }
}
