package com.batodev.arrows.engine

import android.graphics.Bitmap
import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)

class GameEngineShapeLogicTest {

    @Test
    fun `test getRandomShape is called for large boards when probability rolls high`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher()
        val repo = mock<UserPreferencesRepository> {
            on { isVibrationEnabled } doReturn MutableStateFlow(false)
            on { isFillBoardEnabled } doReturn MutableStateFlow(false)
            on { isSoundsEnabled } doReturn MutableStateFlow(false)
            on { levelNumber } doReturn MutableStateFlow(100) // 25x24 board
            on { animationSpeed } doReturn MutableStateFlow("Medium")
            on { initialLevel } doReturn MutableStateFlow(null)
            on { currentLevel } doReturn MutableStateFlow(null)
            on { currentLives } doReturn MutableStateFlow(5)
        }
        
        val shapeProvider = mock<BoardShapeProvider>()
        val generator = mock<GameGenerator>()
        val random = mock<Random> {
            on { nextFloat() } doReturn 0.5f // Less than 0.6
        }
        
        val engine = GameEngine(
            coroutineScope = CoroutineScope(testDispatcher),
            repository = repo,
            gameGenerator = generator,
            autoLoad = false,
            backgroundDispatcher = testDispatcher,
            shapeProvider = shapeProvider,
            random = random
        )
        
        engine.regenerateLevel()
        
        verify(shapeProvider, times(1)).getRandomShape()
    }

    @Test
    fun `test getRandomShape is NOT called for small boards`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher()
        val repo = mock<UserPreferencesRepository> {
            on { isVibrationEnabled } doReturn MutableStateFlow(false)
            on { isFillBoardEnabled } doReturn MutableStateFlow(false)
            on { isSoundsEnabled } doReturn MutableStateFlow(false)
            on { levelNumber } doReturn MutableStateFlow(1) // 5x5 board
            on { animationSpeed } doReturn MutableStateFlow("Medium")
            on { initialLevel } doReturn MutableStateFlow(null)
            on { currentLevel } doReturn MutableStateFlow(null)
            on { currentLives } doReturn MutableStateFlow(5)
        }
        
        val shapeProvider = mock<BoardShapeProvider>()
        val generator = mock<GameGenerator>()
        val random = mock<Random> {
            on { nextFloat() } doReturn 0.1f
        }
        
        val engine = GameEngine(
            coroutineScope = CoroutineScope(testDispatcher),
            repository = repo,
            gameGenerator = generator,
            autoLoad = false,
            backgroundDispatcher = testDispatcher,
            shapeProvider = shapeProvider,
            random = random
        )
        
        engine.regenerateLevel()
        
        verify(shapeProvider, never()).getRandomShape()
    }

    @Test
    fun `test getRandomShape is NOT called when probability rolls low`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher()
        val repo = mock<UserPreferencesRepository> {
            on { isVibrationEnabled } doReturn MutableStateFlow(false)
            on { isFillBoardEnabled } doReturn MutableStateFlow(false)
            on { isSoundsEnabled } doReturn MutableStateFlow(false)
            on { levelNumber } doReturn MutableStateFlow(100) // 25x24 board
            on { animationSpeed } doReturn MutableStateFlow("Medium")
            on { initialLevel } doReturn MutableStateFlow(null)
            on { currentLevel } doReturn MutableStateFlow(null)
            on { currentLives } doReturn MutableStateFlow(5)
        }
        
        val shapeProvider = mock<BoardShapeProvider>()
        val generator = mock<GameGenerator>()
        val random = mock<Random> {
            on { nextFloat() } doReturn 0.7f // More than 0.6
        }
        
        val engine = GameEngine(
            coroutineScope = CoroutineScope(testDispatcher),
            repository = repo,
            gameGenerator = generator,
            autoLoad = false,
            backgroundDispatcher = testDispatcher,
            shapeProvider = shapeProvider,
            random = random
        )
        
        engine.regenerateLevel()
        
        verify(shapeProvider, never()).getRandomShape()
    }
}
