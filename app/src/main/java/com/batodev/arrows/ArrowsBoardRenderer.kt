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
        selectedSnakeId: Int? = null,
        modifier: Modifier = Modifier
    ) {
        Canvas(modifier = modifier) {
            val cellWidth = size.width / level.width
            val cellHeight = size.height / level.height
            val strokeWidth = cellWidth * 0.15f

            // Radius for the curved corners
            val cornerRadius = cellWidth * 0.3f

            // The visual size of the arrow head
            val arrowSize = strokeWidth

            level.snakes.forEach { snake ->
                val path = Path()
                val body = snake.body
                val snakeColor = if (snake.id == selectedSnakeId) Color.Red else Color.Black

                // Calculate common coordinates for the head
                val head = body.first()
                val headCx = head.x * cellWidth + cellWidth / 2
                val headCy = head.y * cellHeight + cellHeight / 2

                // 1. Determine where the line should end (The Base of the Arrow)
                // We push the "end" of the line forward by cornerRadius so the curve happens *at* the cell center
                val lineEndX = headCx + snake.headDirection.dx * cornerRadius
                val lineEndY = headCy + snake.headDirection.dy * cornerRadius

                if (body.size > 1) {
                    // Start at the tail
                    val last = body.last()
                    path.moveTo(
                        last.x * cellWidth + cellWidth / 2,
                        last.y * cellHeight + cellHeight / 2
                    )

                    // Draw segments up to the block *before* the head
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

                    // 2. Connect the final segment to the Arrow Base with a curve
                    val prev = body[1] // The block before head
                    // Entry point into the head cell (from the previous block)
                    // We look backwards from head to prev
                    val headEntryX = headCx + (prev.x - head.x).coerceIn(-1, 1) * cornerRadius
                    val headEntryY = headCy + (prev.y - head.y).coerceIn(-1, 1) * cornerRadius

                    path.lineTo(headEntryX, headEntryY)
                    // Curve from entry -> Center -> Arrow Base (lineEndX, lineEndY)
                    path.quadraticTo(headCx, headCy, lineEndX, lineEndY)

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

                // 3. Handle Single Block Snakes
                if (body.size == 1) {
                    val tailLength = cellWidth * singleBlockTailFactor
                    // Tail starts behind the "line end"
                    // We maintain the shift logic so single blocks look aligned with multi-blocks
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

                // 4. Draw Arrow Head
                // The triangle's logical center needs to be shifted so its "Base" sits at lineEndX/Y
                // Base is located at -0.5 * size from Center.
                // So Center = Base + 0.5 * size
                val triangleCenterX = lineEndX + snake.headDirection.dx * (arrowSize * 0.5f)
                val triangleCenterY = lineEndY + snake.headDirection.dy * (arrowSize * 0.5f)

                drawArrowHead(
                    centerX = triangleCenterX,
                    centerY = triangleCenterY,
                    direction = snake.headDirection,
                    size = arrowSize,
                    color = snakeColor
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

        val angleOffset = 2.094 // ~120 degrees in radians

        val path = Path().apply {
            moveTo(
                centerX + (size * cos(angle)).toFloat(),
                centerY + (size * sin(angle)).toFloat()
            )
            lineTo(
                centerX + (size * cos(angle + angleOffset)).toFloat(),
                centerY + (size * sin(angle + angleOffset)).toFloat()
            )
            lineTo(
                centerX + (size * cos(angle - angleOffset)).toFloat(),
                centerY + (size * sin(angle - angleOffset)).toFloat()
            )
            close()
        }

        drawPath(path, color)

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