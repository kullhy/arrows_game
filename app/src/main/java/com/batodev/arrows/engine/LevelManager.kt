package com.batodev.arrows.engine

import com.batodev.arrows.data.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

private const val MIN_BOARD_SIZE_FOR_SHAPES = 20
private const val MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE = 100
private const val BASE_SHAPE_PROBABILITY = 0.5f

data class LevelConfiguration(val width: Int, val height: Int, val maxSnakeLength: Int, val maxLives: Int)

class LevelManager(
    private val repository: UserPreferencesRepository,
    private val gameGenerator: GameGenerator,
    private val shapeProvider: BoardShapeProvider?,
    private val random: kotlin.random.Random,
    private val gson: Gson
) {

    suspend fun loadLevel(
        onSuccess: (initial: GameLevel, current: GameLevel, maxLives: Int, currentLives: Int) -> Unit,
        onFailure: suspend () -> Unit
    ) {
        val savedInitial = repository.initialLevel.firstOrNull()
        val savedCurrent = repository.currentLevel.firstOrNull()
        val savedLives = repository.currentLives.firstOrNull()

        if (savedInitial != null && savedCurrent != null) {
            try {
                val currentLevelNum = repository.levelNumber.firstOrNull() ?: 1
                val config = LevelProgression.calculateLevelConfiguration(currentLevelNum)

                val initialLevel = gson.fromJson(savedInitial, GameLevel::class.java)
                val currentLevel = gson.fromJson(savedCurrent, GameLevel::class.java)
                
                onSuccess(initialLevel, currentLevel, config.maxLives, savedLives ?: config.maxLives)
            } catch (_: Exception) {
                onFailure()
            }
        } else {
            onFailure()
        }
    }

    suspend fun regenerateLevel(params: RegenerationParams) {
        val currentLevelNum = repository.levelNumber.firstOrNull() ?: 1

        val config = LevelProgression.calculateLevelConfiguration(
            currentLevelNum, params.forcedWidth, params.forcedHeight, params.forcedLives
        )

        val shape = determineShape(config, params.forcedShape)

        val newLevel = gameGenerator.generateSolvableLevel(
            GenerationParams(
                width = config.width, height = config.height, maxSnakeLength = config.maxSnakeLength,
                onProgress = params.onProgress, boardShape = shape
            )
        )

        withContext(Dispatchers.Main) {
            params.onComplete(newLevel, config)
        }
    }

    private fun determineShape(config: LevelConfiguration, forcedShape: String?): BoardShape? {
        return if (forcedShape != null) {
            shapeProvider?.getShapeByName(forcedShape)
        } else if (shouldApplyShape(config)) {
            shapeProvider?.getRandomShape()
        } else {
            null
        }
    }

    private fun shouldApplyShape(config: LevelConfiguration): Boolean {
        val size = maxOf(config.width, config.height)
        val probability = when {
            size < MIN_BOARD_SIZE_FOR_SHAPES -> 0f
            size >= MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE -> 1f
            else -> {
                val ratio = (size - MIN_BOARD_SIZE_FOR_SHAPES).toFloat() /
                        (MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE - MIN_BOARD_SIZE_FOR_SHAPES)
                BASE_SHAPE_PROBABILITY + (1f - BASE_SHAPE_PROBABILITY) * ratio
            }
        }

        return random.nextFloat() < probability
    }

    suspend fun saveState(level: GameLevel, lives: Int) {
        repository.saveCurrentLevel(gson.toJson(level))
        repository.saveCurrentLives(lives)
    }
    
    suspend fun saveInitialState(initialLevel: GameLevel, lives: Int) {
        val json = gson.toJson(initialLevel)
        repository.saveInitialLevel(json)
        repository.saveCurrentLevel(json)
        repository.saveCurrentLives(lives)
    }

    suspend fun advanceLevel(currentLevelNum: Int) {
         repository.saveLevelNumber(currentLevelNum + 1)
         repository.clearSavedLevel()
    }
}
