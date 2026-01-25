package com.batodev.arrows.engine

import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@OptIn(ExperimentalCoroutinesApi::class)
class LevelProgressionTest {

    @Test
    fun testLevelProgressionRules() {
        val testDispatcher = UnconfinedTestDispatcher()
        val repo = mock<UserPreferencesRepository>()
        val engine = GameEngine(
            coroutineScope = CoroutineScope(testDispatcher),
            repository = repo,
            autoLoad = false
        )

        // Test L1: Base 5x5, Step 0. Reduction 0. Expected 5x5, 5 lives.
        verifyLevel(engine, 1, expectedW = 5, expectedH = 5, expectedLives = 5)

        // Test L10: Base 10x9, Step 1. Reduction 3. Expected 7x6, 4 lives.
        verifyLevel(engine, 10, expectedW = 7, expectedH = 6, expectedLives = 4)

        // Test L11: Base 10x10, Step 1. Reduction 3. Expected 7x7, 4 lives.
        verifyLevel(engine, 11, expectedW = 7, expectedH = 7, expectedLives = 4)

        // Test L20: Base 15x14, Step 2. Reduction 6. Expected 9x8, 3 lives.
        // BaseW: 5 + 20/2 = 15. BaseH: 5 + 19/2 = 14. Reduction: 2 * 3 = 6. 
        // 15-6=9, 14-6=8. Lives: 5 - 2 = 3.
        verifyLevel(engine, 20, expectedW = 9, expectedH = 8, expectedLives = 3)

        // Test L50: Lives should be at least 1.
        // BaseW: 5 + 50/2 = 30. BaseH: 5 + 49/2 = 29. Reduction: 5 * 3 = 15.
        // 30-15=15, 29-15=14. Lives: 5 - 5 = 0 -> Coerced to 1.
        verifyLevel(engine, 50, expectedW = 15, expectedH = 14, expectedLives = 1)
    }

    private fun verifyLevel(engine: GameEngine, levelNum: Int, expectedW: Int, expectedH: Int, expectedLives: Int) {
        val calcFunc = engine::class.declaredMemberFunctions.find { it.name == "calculateLevelConfiguration" }!!
        calcFunc.isAccessible = true
        val config = calcFunc.call(engine, levelNum)!!
        
        // Get properties from the private data class via reflection
        val configClass = config::class
        val widthProp = configClass.memberProperties.find { it.name == "width" }!!
        val heightProp = configClass.memberProperties.find { it.name == "height" }!!
        val maxLivesProp = configClass.memberProperties.find { it.name == "maxLives" }!!
        
        widthProp.isAccessible = true
        heightProp.isAccessible = true
        maxLivesProp.isAccessible = true

        val width = widthProp.call(config) as Int
        val height = heightProp.call(config) as Int
        val maxLives = maxLivesProp.call(config) as Int

        assertEquals("Level $levelNum Width mismatch", expectedW, width)
        assertEquals("Level $levelNum Height mismatch", expectedH, height)
        assertEquals("Level $levelNum Lives mismatch", expectedLives, maxLives)
    }
}
