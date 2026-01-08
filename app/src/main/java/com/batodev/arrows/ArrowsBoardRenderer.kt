package com.batodev.arrows

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameLevel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val singleBlockTailFactor: Float = 0.2f

object ArrowsBoardRenderer {
    @Composable
    fun Board(
        level: GameLevel,
        modifier: Modifier = Modifier
    ) {
        Canvas(modifier = modifier) {
            val cellWidth = size.width / level.width
            val cellHeight = size.height / level.height
            val strokeWidth = cellWidth * 0.15f

            level.snakes.forEach { snake ->
                val path = Path()
                val first = snake.body.first()
                path.moveTo(
                    first.x * cellWidth + cellWidth / 2,
                    first.y * cellHeight + cellHeight / 2
                )
                for (i in 1 until snake.body.size) {
                    val p = snake.body[i]
                    path.lineTo(
                        p.x * cellWidth + cellWidth / 2,
                        p.y * cellHeight + cellHeight / 2
                    )
                }
                // Draw the snake body (for multi-segment only)
                if (snake.body.size > 1) {
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
                val head = snake.body.first()
                val headX = head.x * cellWidth + cellWidth / 2
                val headY = head.y * cellHeight + cellHeight / 2
                if (snake.body.size == 1) {
                    // Draw a short tail for 1-block arrows
                    val tailLength = cellWidth * singleBlockTailFactor
                    val tailX = headX - snake.headDirection.dx * tailLength
                    val tailY = headY - snake.headDirection.dy * tailLength
                    drawLine(
                        color = Color.Black,
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
                drawArrowHead(
                    centerX = headX,
                    centerY = headY,
                    direction = snake.headDirection,
                    size = strokeWidth,
                    color = Color.Black
                )
            }
        }
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowHead(
        centerX: Float,
        centerY: Float,
        direction: Direction,
        size: Float,
        color: Color
    ) {
        val angle = when (direction) {
            Direction.UP -> 270.0
            Direction.DOWN -> 90.0
            Direction.LEFT -> 180.0
            Direction.RIGHT -> 0.0
        } * (PI / 180.0)

        // Create equilateral triangle with equal sides
        // 120 degrees (2.094 radians) between each vertex for equal sides
        val angleOffset = 2.094 // ~120 degrees in radians

        val path = Path().apply {
            // Tip of the arrow
            moveTo(
                centerX + (size * cos(angle)).toFloat(),
                centerY + (size * sin(angle)).toFloat()
            )
            // Back left wing
            lineTo(
                centerX + (size * cos(angle + angleOffset)).toFloat(),
                centerY + (size * sin(angle + angleOffset)).toFloat()
            )
            // Back right wing
            lineTo(
                centerX + (size * cos(angle - angleOffset)).toFloat(),
                centerY + (size * sin(angle - angleOffset)).toFloat()
            )
            close()
        }

        // Draw filled triangle
        drawPath(path, color)

        // Draw rounded stroke over the edges for rounded corners
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = size * 0.3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
