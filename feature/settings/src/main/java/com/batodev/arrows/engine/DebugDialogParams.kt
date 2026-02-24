package com.batodev.arrows.engine

import com.batodev.arrows.ui.AppViewModel

data class DebugDialogParams(
    val dialogToShow: String?,
    val viewModel: AppViewModel,
    val levelNumber: Int,
    val forcedWidth: Int?,
    val forcedHeight: Int?,
    val forcedLives: Int?,
    val forcedShape: String?,
    val onDismiss: () -> Unit
)
