package com.batodev.arrows.engine

import androidx.compose.ui.geometry.Offset

data class TapTransformationParams(
    val tapOffset: Offset,
    val containerWidth: Float,
    val containerHeight: Float,
    val level: GameLevel,
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float
)

data class RegenerationParams(
    val forcedWidth: Int?,
    val forcedHeight: Int?,
    val forcedLives: Int?,
    val forcedShape: String?,
    val onProgress: (Float) -> Unit,
    val onComplete: (GameLevel, LevelConfiguration) -> Unit
)

data class LoSParams(
    val head: Point,
    val dir: Direction,
    val sId: Int,
    val grid: Array<IntArray>,
    val w: Int,
    val h: Int
)
