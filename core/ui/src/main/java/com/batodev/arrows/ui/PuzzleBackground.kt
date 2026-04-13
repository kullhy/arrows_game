@file:Suppress("MagicNumber")

package com.batodev.arrows.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.batodev.arrows.ui.theme.LocalThemeColors

private const val GRAIN_ALPHA = 0.06f
private const val GRID_ALPHA = 0.08f
private const val STAGGERED_DOT_OFFSET_DIVISOR = 3f

@Composable
fun PuzzleBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val themeColors = LocalThemeColors.current
    Box(modifier = modifier.background(themeColors.background)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawPaperGrain()
            drawPuzzleGrid()
        }
        content()
    }
}

private fun DrawScope.drawPaperGrain() {
    val speck = Color(0xFF7B623C).copy(alpha = GRAIN_ALPHA)
    val step = 18.dp.toPx()
    var y = step / 2f
    var row = 0
    while (y < size.height) {
        var x = step / 2f + if (row % 2 == 0) 0f else step / STAGGERED_DOT_OFFSET_DIVISOR
        while (x < size.width) {
            drawCircle(color = speck, radius = 0.8.dp.toPx(), center = Offset(x, y))
            x += step
        }
        row++
        y += step
    }
}

private fun DrawScope.drawPuzzleGrid() {
    val lineColor = Color(0xFF9C7A48).copy(alpha = GRID_ALPHA)
    val step = 56.dp.toPx()
    var x = 0f
    while (x <= size.width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height))
        x += step
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y))
        y += step
    }
}
