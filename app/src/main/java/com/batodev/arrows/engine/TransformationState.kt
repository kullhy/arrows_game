package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

private const val MIN_SCALE = 0.2f
private const val MAX_SCALE = 6f

class TransformationState {
    var scale by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)

    fun reset() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    fun transform(pan: Offset, zoom: Float) {
        scale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
        offsetX += pan.x
        offsetY += pan.y
    }
}
