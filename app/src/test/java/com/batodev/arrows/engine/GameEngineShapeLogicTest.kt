package com.batodev.arrows.engine

import android.graphics.Bitmap
import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
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
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val repo = FakeUserPreferencesRepository()
        repo.saveLevelNumber(100) // 25x24 board
        
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
        
        runCurrent()
        engine.regenerateLevel()
        runCurrent()
        
        verify(shapeProvider, times(1)).getRandomShape()
    }

    @Test
    fun `test getRandomShape is NOT called for small boards`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val repo = FakeUserPreferencesRepository()
        repo.saveLevelNumber(1) // 5x5 board
        
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
        
        runCurrent()
        engine.regenerateLevel()
        runCurrent()
        
        verify(shapeProvider, never()).getRandomShape()
    }

    @Test
    fun `test getRandomShape is NOT called when probability rolls low`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val repo = FakeUserPreferencesRepository()
        repo.saveLevelNumber(100) // 25x24 board
        
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
        
        runCurrent()
        engine.regenerateLevel()
        runCurrent()
        
        verify(shapeProvider, never()).getRandomShape()
    }
}
