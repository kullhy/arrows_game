package com.batodev.arrows

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

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
    // Draw a small expanding circle that fades out at the exact tap location
    // Max radius 40px, fades to alpha 0

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxRadius = 40f
        val radius = maxRadius * value
        drawCircle(
            color = Color.White.copy(alpha = 1f - value),
            radius = radius,
            center = offset
        )
    }
}
