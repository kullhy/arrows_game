package com.batodev.arrows

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

data class TapAnimationState(val id: Long, val offset: Offset)

@Composable
fun TapRipple(
    offset: Offset,
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(300))
        onFinished()
    }

    val value = progress.value
    // Draw a small expanding circle that fades out
    // Max radius 40px, fades to alpha 0
    
    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt() - 40, offset.y.toInt() - 40) }
            .size(80.dp) // Container size
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2 * value
            drawCircle(
                color = Color.White.copy(alpha = 1f - value),
                radius = radius,
                center = center
            )
        }
    }
}
