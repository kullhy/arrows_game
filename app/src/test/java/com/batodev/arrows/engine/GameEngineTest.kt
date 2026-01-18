package com.batodev.arrows.engine

import androidx.compose.ui.geometry.Offset
import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class FakeUserPreferencesRepository : UserPreferencesRepository(mock()) {
    val themeFlow = MutableStateFlow("Dark")
    val initialLevelFlow = MutableStateFlow<String?>(null)
    val currentLevelFlow = MutableStateFlow<String?>(null)

    override val theme: Flow<String> = themeFlow
    override val initialLevel: Flow<String?> = initialLevelFlow
    override val currentLevel: Flow<String?> = currentLevelFlow

    override suspend fun saveThemePreference(theme: String) { themeFlow.value = theme }
    override suspend fun saveInitialLevel(levelJson: String) { initialLevelFlow.value = levelJson }
    override suspend fun saveCurrentLevel(levelJson: String) { currentLevelFlow.value = levelJson }
    override suspend fun clearSavedLevel() {
        initialLevelFlow.value = null
        currentLevelFlow.value = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GameEngineTest {

    private val repository = FakeUserPreferencesRepository()
    private val gameGenerator: GameGenerator = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(gameGenerator.generateSolvableLevel(any(), any(), any(), any(), any())).thenReturn(
            GameLevel(1, 1, emptyList())
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state and level generation`() = runTest {
        val level = GameLevel(5, 5, listOf(Snake(1, listOf(Point(0, 0)), Direction.RIGHT)))
        whenever(gameGenerator.generateSolvableLevel(any(), any(), any(), any(), any())).thenReturn(level)

        val engine = GameEngine(this, repository, gameGenerator, autoLoad = false, backgroundDispatcher = UnconfinedTestDispatcher(testScheduler))
        engine.loadOrRegenerateLevel()
        
        assertEquals(level, engine.level)
        assertFalse(engine.isLoading)
        assertEquals(3, engine.lives)
    }

    @Test
    fun `test tap on obstructed snake deducts life`() = runTest {
        val snake1 = Snake(1, listOf(Point(0, 0)), Direction.RIGHT)
        val snake2 = Snake(2, listOf(Point(1, 0)), Direction.DOWN)
        val level = GameLevel(5, 5, listOf(snake1, snake2))
        
        whenever(gameGenerator.generateSolvableLevel(any(), any(), any(), any(), any())).thenReturn(level)

        val engine = GameEngine(this, repository, gameGenerator, autoLoad = false, backgroundDispatcher = UnconfinedTestDispatcher(testScheduler))
        engine.loadOrRegenerateLevel()

        // Target (160, 100) for head (0,0) with size 1000f
        engine.onTap(Offset(160f, 100f), 1000f)
        
        assertEquals(2, engine.lives)
        assertEquals(1, engine.flashingSnakeId)
    }

    @Test
    fun `test addLife works correctly`() = runTest {
        val snake1 = Snake(1, listOf(Point(0, 0)), Direction.RIGHT)
        val snake2 = Snake(2, listOf(Point(1, 0)), Direction.DOWN)
        val levelWithSnakes = GameLevel(2, 2, listOf(snake1, snake2))
        whenever(gameGenerator.generateSolvableLevel(any(), any(), any(), any(), any())).thenReturn(levelWithSnakes)
        
        val engine = GameEngine(this, repository, gameGenerator, autoLoad = false, backgroundDispatcher = UnconfinedTestDispatcher(testScheduler))
        engine.loadOrRegenerateLevel()
        
        // Obstructed tap
        engine.onTap(Offset(400f, 250f), 1000f) 
        assertEquals(2, engine.lives)
        
        engine.addLife()
        assertEquals(3, engine.lives)
    }

    @Test
    fun `test restartLevel resets state`() = runTest {
        val snake = Snake(1, listOf(Point(0, 0)), Direction.RIGHT)
        val level = GameLevel(5, 5, listOf(snake))
        whenever(gameGenerator.generateSolvableLevel(any(), any(), any(), any(), any())).thenReturn(level)

        val engine = GameEngine(this, repository, gameGenerator, autoLoad = false, backgroundDispatcher = UnconfinedTestDispatcher(testScheduler))
        engine.loadOrRegenerateLevel()

        engine.onTransform(Offset(10f, 10f), 2f)
        engine.restartLevel()
        
        assertEquals(1f, engine.scale)
        assertEquals(level, engine.level)
    }

    @Test
    fun `test clearSavedLevel is called when game is won`() = runTest {
        // Level with 1 snake
        val level = GameLevel(5, 5, listOf(Snake(1, listOf(Point(0, 0)), Direction.RIGHT)))
        whenever(gameGenerator.generateSolvableLevel(any(), any(), any(), any(), any())).thenReturn(level)

        val engine = GameEngine(this, repository, gameGenerator, autoLoad = false, backgroundDispatcher = UnconfinedTestDispatcher(testScheduler))
        engine.loadOrRegenerateLevel()
        
        // Ensure it's saved initially
        assertNotNull(repository.initialLevelFlow.value)
        
        // Tap to remove the only snake
        // Target (160, 100) for head (0,0)
        engine.onTap(Offset(160f, 100f), 1000f)
        
        // Wait for animation and removal
        // Note: animateRemoval has loops and delays. In UnconfinedTestDispatcher with runTest, 
        // delays are handled by virtual time.
        advanceUntilIdle()

        assertTrue(engine.isGameWon)
        assertNull(repository.initialLevelFlow.value)
        assertNull(repository.currentLevelFlow.value)
    }
}
