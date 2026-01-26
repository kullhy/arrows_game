package com.batodev.arrows.ui

import com.batodev.arrows.engine.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)

class AppViewModelTest {

    private val repository = FakeUserPreferencesRepository()
    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AppViewModel(repository)
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
    fun `test saveDebugForcedWidth updates state`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.debugForcedWidth.collect {}
        }
        
        viewModel.saveDebugForcedWidth(10)
        assertEquals(10, repository.debugForcedWidthFlow.value)
        assertEquals(10, viewModel.debugForcedWidth.value)
        
        collectJob.cancel()
    }

    @Test
    fun `test saveDebugForcedShape updates state`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.debugForcedShape.collect {}
        }
        
        viewModel.saveDebugForcedShape("heart")
        assertEquals("heart", repository.debugForcedShapeFlow.value)
        assertEquals("heart", viewModel.debugForcedShape.value)
        
        collectJob.cancel()
    }

    @Test
    fun `test regenerateCurrentLevel clears saved level`() = runTest {
        repository.saveInitialLevel("{}")
        repository.saveCurrentLevel("{}")
        
        viewModel.regenerateCurrentLevel()
        
        assertNull(repository.initialLevelFlow.value)
        assertNull(repository.currentLevelFlow.value)
    }
}
