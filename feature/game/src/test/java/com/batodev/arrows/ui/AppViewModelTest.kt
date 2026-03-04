package com.batodev.arrows.ui

import com.batodev.arrows.core.testing.FakeGameStateDao
import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)

class AppViewModelTest {

    private val repository = FakeUserPreferencesRepository()
    private val gameStateDao = FakeGameStateDao()
    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AppViewModel(repository, gameStateDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial theme is Dark`() = runTest {
        assertEquals("Dark", viewModel.theme.value)
    }

    @Test
    fun `test saveTheme updates state`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.theme.collect {}
        }

        viewModel.saveTheme("Light")
        assertEquals("Light", repository.themeFlow.value)
        assertEquals("Light", viewModel.theme.value)

        collectJob.cancel()
    }

    @Test
    fun `test saveDebugOption width updates state`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.debugForcedWidth.collect {}
        }

        viewModel.saveDebugOption(AppViewModel.DebugOption.WIDTH, 10)
        assertEquals(10, repository.debugForcedWidthFlow.value)
        assertEquals(10, viewModel.debugForcedWidth.value)

        collectJob.cancel()
    }

    @Test
    fun `test saveDebugOption shape updates state`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.debugForcedShape.collect {}
        }

        viewModel.saveDebugOption(AppViewModel.DebugOption.SHAPE, "heart")
        assertEquals("heart", repository.debugForcedShapeFlow.value)
        assertEquals("heart", viewModel.debugForcedShape.value)

        collectJob.cancel()
    }

    @Test
    fun `test regenerateCurrentLevel clears saved level`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hasSavedLevel.collect {}
        }

        gameStateDao.saveGameLevel("INITIAL", 5, 5, emptyList())
        gameStateDao.saveGameLevel("CURRENT", 5, 5, emptyList())
        assertTrue(viewModel.hasSavedLevel.value)

        viewModel.regenerateCurrentLevel()

        assertFalse(viewModel.hasSavedLevel.value)

        collectJob.cancel()
    }
}
