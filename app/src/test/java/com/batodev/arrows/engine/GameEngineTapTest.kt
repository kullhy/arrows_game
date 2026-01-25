package com.batodev.arrows.engine

import androidx.compose.ui.geometry.Offset
import com.batodev.arrows.data.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)

class GameEngineTapTest {

    @Test
    fun `test closest snake is selected within tolerance`() {
        val gson = Gson()
        // Setup levels
        // Snake 1 (1,1) RIGHT -> target center roughly (1.8, 1.5)
        // Snake 2 (3,1) LEFT -> target center roughly (3.2, 1.5)
        val snake1 = Snake(1, listOf(Point(1, 1)), Direction.RIGHT)
        val snake2 = Snake(2, listOf(Point(3, 1)), Direction.LEFT)
        val level = GameLevel(5, 5, listOf(snake1, snake2))
        val levelJson = gson.toJson(level)

        // Mock repo
        val repo = mock<UserPreferencesRepository> {
            on { initialLevel } doReturn MutableStateFlow(levelJson)
            on { currentLevel } doReturn MutableStateFlow(levelJson)
            on { isVibrationEnabled } doReturn MutableStateFlow(false)
            on { isSoundsEnabled } doReturn MutableStateFlow(true)
            on { isFillBoardEnabled } doReturn MutableStateFlow(false)
            on { levelNumber } doReturn MutableStateFlow(1)
            on { currentLives } doReturn MutableStateFlow(5)
            on { animationSpeed } doReturn MutableStateFlow("Medium")
        }

        val testDispatcher = UnconfinedTestDispatcher()
        val engine = GameEngine(
            coroutineScope = CoroutineScope(testDispatcher),
            repository = repo,
            autoLoad = false,
            backgroundDispatcher = testDispatcher
        )

        // Load level
        engine.loadOrRegenerateLevel()

        // Ensure level loaded
        assertEquals(2, engine.level.snakes.size)

        // Board setup
        val boardSize = 500f
        val cellWidth = boardSize / 5f // 100f

        // Target centers (calculated based on TAP_AREA_OFFSET_FACTOR = 0.3)
        // Snake 1: x = 1.0 + 0.5 + 0.3 = 1.8.  y = 1.5.
        // Snake 2: x = 3.0 + 0.5 - 0.3 = 3.2.  y = 1.5.

                // Tap closer to Snake 1
                // Tap at x = 2.4. 
                // Dist to S1 (1.8): 0.6.
                // Dist to S2 (3.2): 0.8.
                // Both are within tolerance 1.3.
                // Should pick S1.
                
                val tapOffset = Offset(2.4f * cellWidth, 1.5f * cellWidth)
                engine.onTap(tapOffset, boardSize, boardSize)
                
                // Check if S1 flashed (id 1)
                assertEquals("Should have picked snake 1", 1, engine.flashingSnakeId)
                
                // Reset flashing
                engine.restartLevel()
                
                // Tap closer to Snake 2
                // Tap at x = 2.6.
                // Dist to S1 (1.8): 0.8.
                // Dist to S2 (3.2): 0.6.
                // Should pick S2.
                
                val tapOffset2 = Offset(2.6f * cellWidth, 1.5f * cellWidth)
                engine.onTap(tapOffset2, boardSize, boardSize)
                
                // S2 should flash (id 2)
                assertEquals("Should have picked snake 2", 2, engine.flashingSnakeId)
            }
        }
