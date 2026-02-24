package com.batodev.arrows.engine

data class GenerationParams(
    val width: Int,
    val height: Int,
    val maxSnakeLength: Int,
    val fillTheBoard: Boolean = false,
    val boardShape: BoardShape? = null,
    val onProgress: (Float) -> Unit = {},
)
