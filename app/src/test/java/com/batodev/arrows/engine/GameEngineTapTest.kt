package com.batodev.arrows.engine

import androidx.compose.ui.geometry.Offset
import com.batodev.arrows.data.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
    fun `test closest snake is selected within tolerance`() = runTest {
        val gson = Gson()
        // Setup levels
        // Snake 1 (1,1) RIGHT -> target center roughly (1.8, 1.5)
        // Snake 2 (3,1) LEFT -> target center roughly (3.2, 1.5)
        val snake1 = Snake(1, listOf(Point(1, 1)), Direction.RIGHT)
        val snake2 = Snake(2, listOf(Point(3, 1)), Direction.LEFT)
        val level = GameLevel(5, 5, listOf(snake1, snake2))
        val levelJson = gson.toJson(level)

        // Mock repo
        val repo = FakeUserPreferencesRepository()
        repo.saveInitialLevel(levelJson)
        repo.saveCurrentLevel(levelJson)
        repo.saveVibrationPreference(false)
        repo.saveSoundsPreference(true)
        repo.saveFillBoardPreference(false)
        repo.saveLevelNumber(1)
        repo.saveCurrentLives(5)
        repo.saveAnimationSpeed("Medium")

        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val engine = GameEngine(
            coroutineScope = CoroutineScope(testDispatcher),
            repository = repo,
            autoLoad = false,
            backgroundDispatcher = testDispatcher
        )

        // Load level
        engine.loadOrRegenerateLevel()
        runCurrent()

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
                runCurrent()
                
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

    @Test
    fun `test animating snake does not obstruct others`() = runTest {
        val gson = Gson()
        // S1 is at (1,1) moving RIGHT.
        // S2 is at (3,1) moving RIGHT.
        // S1 is obstructed by S2.
        val s1 = Snake(1, listOf(Point(1, 1)), Direction.RIGHT)
        val s2 = Snake(2, listOf(Point(3, 1)), Direction.RIGHT)
        val level = GameLevel(5, 5, listOf(s1, s2))
        val levelJson = gson.toJson(level)

        val repo = FakeUserPreferencesRepository()
        repo.saveInitialLevel(levelJson)
        repo.saveCurrentLevel(levelJson)
        repo.saveVibrationPreference(false)
        repo.saveSoundsPreference(false)
        repo.saveFillBoardPreference(false)
        repo.saveLevelNumber(1)
        repo.saveCurrentLives(5)
        repo.saveAnimationSpeed("Medium")

        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val engine = GameEngine(
            coroutineScope = CoroutineScope(testDispatcher),
            repository = repo,
            autoLoad = false,
            backgroundDispatcher = testDispatcher
        )

        engine.loadOrRegenerateLevel()
        runCurrent()

        assertEquals("Engine should not be loading", false, engine.isLoading)
        assertEquals("Level should have 2 snakes", 2, engine.level.snakes.size)

        // Board setup
        val boardSize = 500f
        val cellWidth = boardSize / 5f // 100f

        // Tap S1 (obstructed by S2)
        val tapS1 = Offset(1.8f * cellWidth, 1.5f * cellWidth)
        engine.onTap(tapS1, boardSize, boardSize)
        
        assertEquals("S1 should flash when obstructed", 1, engine.flashingSnakeId)
        engine.restartLevel()
        runCurrent()

        // Tap S2 (not obstructed)
        val tapS2 = Offset(3.8f * cellWidth, 1.5f * cellWidth)
        engine.onTap(tapS2, boardSize, boardSize)

        // S2 should be in removal progress
        assert(engine.removalProgress.containsKey(2))

        // Now tap S1 again while S2 is animating
        engine.onTap(tapS1, boardSize, boardSize)

        // S1 should NOT flash, and it should now be in removal progress because S2 is ignored
        assertEquals("S1 should not flash", null, engine.flashingSnakeId)
        assert(engine.removalProgress.containsKey(1))
    }
}
