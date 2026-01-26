package com.batodev.arrows.engine

import android.graphics.Bitmap

data class GenerationParams(
    val width: Int,
    val height: Int,
    val maxSnakeLength: Int,
    val fillTheBoard: Boolean = false,
    val shapeBitmap: Bitmap? = null,
    val onProgress: (Float) -> Unit = {},
)
