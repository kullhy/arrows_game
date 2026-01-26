package com.batodev.arrows.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GameEngineDebugParamsTest {

    @Test
    fun `test forced debug parameters override calculation`() = runTest {
        // Since we moved logic to LevelProgression, we test it directly
        val config = LevelProgression.calculateLevelConfiguration(
            levelNum = 1,
            forcedWidth = 20,
            forcedHeight = 30,
            forcedLives = 10
        )

        assertEquals(20, config.width)
        assertEquals(30, config.height)
        assertEquals(10, config.maxLives)
    }

    @Test
    fun `test forced shape is used in regeneration`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val repo = FakeUserPreferencesRepository()
        repo.saveDebugForcedShape("heart")

        val shapeProvider = mock<BoardShapeProvider>()
        val generator = mock<GameGenerator>()

        val engine = GameEngine(
            config = GameEngineConfig(
                coroutineScope = CoroutineScope(testDispatcher),
                repository = repo,
                gameGenerator = generator,
                autoLoad = false,
                backgroundDispatcher = testDispatcher
            ),
            features = GameEngineFeatures(
                shapeProvider = shapeProvider
            )
        )

        runCurrent()
        engine.regenerateLevel()
        runCurrent()

        verify(shapeProvider).getShapeByName("heart")
        verify(shapeProvider, never()).getRandomShape()
    }
}
