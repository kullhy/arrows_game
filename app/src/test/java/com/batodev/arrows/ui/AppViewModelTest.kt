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

@OptIn(ExperimentalCoroutinesApi::class)
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
}

