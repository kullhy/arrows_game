package com.batodev.arrows.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.batodev.arrows.ui.AppViewModel
import kotlinx.coroutines.CoroutineScope

data class IntroState(val showIntro: Boolean, val onDismiss: () -> Unit)

@Composable
fun rememberIntroState(
    viewModel: AppViewModel,
    isLoading: Boolean,
    coroutineScope: CoroutineScope
): IntroState {
    val introCompleted by viewModel.introCompleted.collectAsState()
    var showIntro by remember { mutableStateOf(false) }
    LaunchedEffect(introCompleted, isLoading) {
        if (!introCompleted && !isLoading) showIntro = true
    }
    val onDismiss: () -> Unit = {
        showIntro = false
        viewModel.saveIntroCompleted(true)
    }
    return IntroState(showIntro, onDismiss)
}
