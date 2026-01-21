package com.batodev.arrows.engine

import androidx.compose.ui.geometry.Offset
import com.batodev.arrows.data.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        // Both are within tolerance 0.85.
        // Should pick S1.
        
        // But S1 is obstructed by S2?
        // S1 (1,1) RIGHT -> path (2,1), (3,1), (4,1).
        // (3,1) is occupied by S2.
        // So S1 is obstructed. It should flash.
        
        // S2 (3,1) LEFT -> path (2,1), (1,1), (0,1).
        // (1,1) is occupied by S1.
        // So S2 is also obstructed.
        
        val tapOffset = Offset(2.4f * cellWidth, 1.5f * cellWidth)
        engine.onTap(tapOffset, boardSize, boardSize, boardSize, boardSize)
        
        // Check if S1 flashed (id 1)
        assertEquals(1, engine.flashingSnakeId)
        
        // Reset flashing (hacky but needed since we can't wait for delay easily in this setup without advanceTimeBy)
        // We can just create a new engine or reset state via restart if mocked correctly.
        // Or just test the other case.
        
        // Tap closer to Snake 2
        // Tap at x = 2.6.
        // Dist to S1 (1.8): 0.8.
        // Dist to S2 (3.2): 0.6.
        // Should pick S2.
        
        val tapOffset2 = Offset(2.6f * cellWidth, 1.5f * cellWidth)
        engine.onTap(tapOffset2, boardSize, boardSize, boardSize, boardSize)
        
        // S2 should flash (id 2)
        assertEquals(2, engine.flashingSnakeId)
    }
}
