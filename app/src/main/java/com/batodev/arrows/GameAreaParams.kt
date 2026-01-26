package com.batodev.arrows

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.ui.theme.ThemeColors

data class GameAreaParams(
    val engine: GameEngine,
    val tapAnimations: SnapshotStateList<TapAnimationState>,
    val guidanceAlpha: Float,
    val showGuidanceLines: Boolean,
    val themeColors: ThemeColors,
    val onToggleGuidance: () -> Unit
)
