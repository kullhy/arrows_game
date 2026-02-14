package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.batodev.arrows.GameConstants

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
        scale = (scale * zoom).coerceIn(GameConstants.MIN_SCALE, GameConstants.MAX_SCALE)
        offsetX += pan.x
        offsetY += pan.y
    }
}
