package com.batodev.arrows.engine

import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GameEngineDebugParamsTest {

    @Test
    fun `test forced debug parameters override calculation`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val repo = FakeUserPreferencesRepository()
        repo.saveDebugForcedWidth(20)
        repo.saveDebugForcedHeight(30)
        repo.saveDebugForcedLives(10)

        val engine = GameEngine(
            config = GameEngineConfig(
                coroutineScope = CoroutineScope(testDispatcher),
                repository = repo,
                autoLoad = false
            )
        )

        // Wait for flows to be collected
        runCurrent()

        val calcFunc = engine::class.declaredMemberFunctions.find { it.name == "calculateLevelConfiguration" }!!
        calcFunc.isAccessible = true
        val config = calcFunc.call(engine, 1)!!

        // Access properties of private data class
        val configClass = config::class
        val widthProp = configClass.members.find { it.name == "width" }!!
        val heightProp = configClass.members.find { it.name == "height" }!!
        val maxLivesProp = configClass.members.find { it.name == "maxLives" }!!

        widthProp.isAccessible = true
        heightProp.isAccessible = true
        maxLivesProp.isAccessible = true

        assertEquals(20, widthProp.call(config) as Int)
        assertEquals(30, heightProp.call(config) as Int)
        assertEquals(10, maxLivesProp.call(config) as Int)
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
