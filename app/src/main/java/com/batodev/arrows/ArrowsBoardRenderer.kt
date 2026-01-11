package com.batodev.arrows

import android.util.Log
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
import kotlin.math.max
import kotlin.system.measureTimeMillis

const val singleBlockTailFactor: Float = 0.2f

const val ARROW_HEAD_SIZE_FACTOR = 0.2f

object ArrowsBoardRenderer {
    @Composable
    fun Board(
        level: GameLevel,
        modifier: Modifier = Modifier,
        flashingSnakeId: Int? = null,
        removalProgress: Map<Int, Float> = emptyMap()
    ) {
        Canvas(modifier = modifier) {
            val totalDrawTime = measureTimeMillis {
                val cellWidth = size.width / level.width
                val cellHeight = size.height / level.height
                val strokeWidth = cellWidth * 0.15f
                val cornerRadius = cellWidth * 0.3f
                val arrowHeadSize = cellWidth * ARROW_HEAD_SIZE_FACTOR

                val moveDist = max(size.width, size.height) * 1.2f

                level.snakes.forEach { snake ->
                    val p = (removalProgress[snake.id] ?: 0f).coerceIn(0f, 1f)
                    val shift = moveDist * p
                    val alpha = 1f - p

                    val snakeDrawTime = measureTimeMillis {
                        val path = Path()
                        val body = snake.body
                        val baseColor = if (snake.id == flashingSnakeId) Color.Red else Color.Black
                        val snakeColor = baseColor.copy(alpha = alpha)

                        val head = body.first()
                        val headCx0 = head.x * cellWidth + cellWidth / 2
                        val headCy0 = head.y * cellHeight + cellHeight / 2

                        // Head moves forward during removal
                        val headCx = headCx0 + snake.headDirection.dx * shift
                        val headCy = headCy0 + snake.headDirection.dy * shift

                        // Arrow base (also moves with head)
                        val lineEndX = headCx + snake.headDirection.dx * cornerRadius
                        val lineEndY = headCy + snake.headDirection.dy * cornerRadius

                        // Original (non-animated) arrow base. We'll draw the normal curved approach into this,
                        // then extend with a straight segment to the moved arrow base.
                        val baseLineEndX0 = headCx0 + snake.headDirection.dx * cornerRadius
                        val baseLineEndY0 = headCy0 + snake.headDirection.dy * cornerRadius

                        if (body.size > 1) {
                            val last = body.last()
                            path.moveTo(
                                last.x * cellWidth + cellWidth / 2,
                                last.y * cellHeight + cellHeight / 2
                            )

                            for (i in body.size - 2 downTo 1) {
                                val prev = body[i + 1]
                                val current = body[i]
                                val next = body[i - 1]

                                val currX = current.x * cellWidth + cellWidth / 2
                                val currY = current.y * cellHeight + cellHeight / 2

                                val entryX = currX + (prev.x - current.x).coerceIn(-1, 1) * cornerRadius
                                val entryY = currY + (prev.y - current.y).coerceIn(-1, 1) * cornerRadius
                                val exitX = currX + (next.x - current.x).coerceIn(-1, 1) * cornerRadius
                                val exitY = currY + (next.y - current.y).coerceIn(-1, 1) * cornerRadius

                                path.lineTo(entryX, entryY)
                                path.quadraticTo(currX, currY, exitX, exitY)
                            }

                            val prev = body[1]
                            val headEntryX = headCx0 + (prev.x - head.x).coerceIn(-1, 1) * cornerRadius
                            val headEntryY = headCy0 + (prev.y - head.y).coerceIn(-1, 1) * cornerRadius

                            path.lineTo(headEntryX, headEntryY)

                            // Keep the original curved approach into the head cell/arrow base.
                            path.quadraticTo(headCx0, headCy0, baseLineEndX0, baseLineEndY0)

                            // Then extend with a straight segment to the moved arrow base (the "link" line).
                            if (p > 0f) {
                                path.lineTo(lineEndX, lineEndY)
                            }

                            drawPath(
                                path = path,
                                color = snakeColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        if (body.size == 1) {
                            val tailLength = cellWidth * singleBlockTailFactor
                            val tailStartX = lineEndX - snake.headDirection.dx * (tailLength + cornerRadius)
                            val tailStartY = lineEndY - snake.headDirection.dy * (tailLength + cornerRadius)

                            drawLine(
                                color = snakeColor,
                                start = Offset(tailStartX, tailStartY),
                                end = Offset(lineEndX, lineEndY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }

                        val triangleCenterX = lineEndX + snake.headDirection.dx * (arrowHeadSize * 0.5f)
                        val triangleCenterY = lineEndY + snake.headDirection.dy * (arrowHeadSize * 0.5f)

                        drawArrowHead(
                            centerX = triangleCenterX,
                            centerY = triangleCenterY,
                            direction = snake.headDirection,
                            arrowHeadSize = arrowHeadSize,
                            color = snakeColor
                        )
                    }

                    Log.v(
                        ArrowsBoardRenderer.javaClass.simpleName,
                        "Snake ${snake.id} draw time: $snakeDrawTime ms"
                    )
                }
            }
            Log.v(ArrowsBoardRenderer.javaClass.simpleName, "Total board draw time: $totalDrawTime ms")
        }
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowHead(
        centerX: Float,
        centerY: Float,
        direction: Direction,
        arrowHeadSize: Float,
        color: Color
    ) {
        val angle = when (direction) {
            Direction.UP -> 270.0
            Direction.DOWN -> 90.0
            Direction.LEFT -> 180.0
            Direction.RIGHT -> 0.0
        } * (PI / 180.0)

        val angleOffset = 2.094 // ~120 degrees in radians

        val path = Path().apply {
            moveTo(
                centerX + (arrowHeadSize * cos(angle)).toFloat(),
                centerY + (arrowHeadSize * sin(angle)).toFloat()
            )
            lineTo(
                centerX + (arrowHeadSize * cos(angle + angleOffset)).toFloat(),
                centerY + (arrowHeadSize * sin(angle + angleOffset)).toFloat()
            )
            lineTo(
                centerX + (arrowHeadSize * cos(angle - angleOffset)).toFloat(),
                centerY + (arrowHeadSize * sin(angle - angleOffset)).toFloat()
            )
            close()
        }

        drawPath(path, color)

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = arrowHeadSize * 0.3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
