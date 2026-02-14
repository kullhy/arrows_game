package com.batodev.arrows.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class IntroState(val showIntro: Boolean, val onDismiss: () -> Unit)

@Composable
fun rememberIntroState(
    repository: UserPreferencesRepository,
    isLoading: Boolean,
    coroutineScope: CoroutineScope
): IntroState {
    val introCompleted by repository.introCompleted.collectAsState(initial = true)
    var showIntro by remember { mutableStateOf(false) }
    LaunchedEffect(introCompleted, isLoading) {
        if (!introCompleted && !isLoading) showIntro = true
    }
    val onDismiss: () -> Unit = {
        showIntro = false
        coroutineScope.launch { repository.saveIntroCompleted(true) }
    }
    return IntroState(showIntro, onDismiss)
}
