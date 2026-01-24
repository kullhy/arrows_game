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
import androidx.compose.ui.unit.dp
import com.batodev.arrows.engine.DEFAULT_TOLERANCE
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameLevel
import com.batodev.arrows.ui.theme.FlashingRed
import com.batodev.arrows.ui.theme.LightGray
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.system.measureTimeMillis

/**
 * Factor to determine the tail length for single-block snakes.
 * The tail length is calculated as: cellWidth * singleBlockTailFactor
 */
const val singleBlockTailFactor: Float = 0.2f

/**
 * Factor to determine the arrow head size relative to cell width.
 * The arrow head size is calculated as: cellWidth * ARROW_HEAD_SIZE_FACTOR
 */
const val ARROW_HEAD_SIZE_FACTOR = 0.2f

/**
 * Factor to determine how much the tap area is shifted in the arrow direction.
 * The offset is calculated as: cellWidth * TAP_AREA_OFFSET_FACTOR
 * This makes it easier to tap arrows by moving the tap target toward the arrow head.
 */
const val TAP_AREA_OFFSET_FACTOR = 0.3f

/**
 * Renderer responsible for drawing the arrows game board including snakes, arrow heads,
 * tap areas, and animations.
 */
object ArrowsBoardRenderer {
    /**
     * Renders the game board with all snakes, arrows, and interactive elements.
     *
     * This composable draws:
     * - Tap areas (light gray circles) showing where users can tap to select snake heads (debug only)
     * - Snake bodies with curved corners at turns
     * - Arrow heads pointing in the snake's direction
     * - Animations for snake removal (moving head, fading, shrinking tail)
     *
     * @param level The game level containing the grid dimensions and snakes to render
     * @param modifier Modifier to be applied to the Canvas
     * @param flashingSnakeId ID of a snake that should be rendered in red (e.g., when obstructed)
     * @param removalProgress Map of snake IDs to their removal animation progress (0.0 to 1.0).
     *                        0.0 = not started, 1.0 = fully removed
     */
    @Composable
    fun Board(
        level: GameLevel,
        modifier: Modifier = Modifier,
        flashingSnakeId: Int? = null,
        removalProgress: Map<Int, Float> = emptyMap(),
        guidanceAlpha: Float = 0f,
    ) {
        val themeColors = com.batodev.arrows.ui.theme.LocalThemeColors.current
        Canvas(modifier = modifier) {
            val totalDrawTime = measureTimeMillis {
                // Calculate uniform cell size to maintain aspect ratio
                val cellSize = kotlin.math.min(size.width / level.width, size.height / level.height)
                val boardWidth = cellSize * level.width
                val boardHeight = cellSize * level.height
                
                // Centering offsets
                val leftOffset = (size.width - boardWidth) / 2
                val topOffset = (size.height - boardHeight) / 2

                val metrics = BoardMetrics(
                    cellWidth = cellSize,
                    cellHeight = cellSize,
                    strokeWidth = cellSize * 0.15f,
                    cornerRadius = cellSize * 0.3f,
                    arrowHeadSize = cellSize * ARROW_HEAD_SIZE_FACTOR,
                    moveDist = max(size.width, size.height) * 1.2f,
                    boardWidth = boardWidth,
                    boardHeight = boardHeight
                )

                // Draw game area border in debug builds
                if (BuildConfig.DRAW_DEBUG_STUFF) {
                    drawDebugBorder(leftOffset, topOffset, metrics)
                }

                drawContext.canvas.save()
                drawContext.canvas.translate(leftOffset, topOffset)

                if (guidanceAlpha > 0f) {
                    drawGuidanceLines(level, metrics, removalProgress, guidanceAlpha, themeColors.accent, size.width, size.height, leftOffset, topOffset)
                }

                // Draw tap areas for snake heads (debug visualization only)
                if (BuildConfig.DRAW_DEBUG_STUFF) {
                    drawDebugTapAreas(level, metrics)
                }

                drawSnakes(level, metrics, removalProgress, flashingSnakeId, themeColors)

                drawContext.canvas.restore()
            }
            // Log the total time taken to draw the board for performance monitoring
            Log.v(
                ArrowsBoardRenderer.javaClass.simpleName,
                "Total board draw time: $totalDrawTime ms"
            )
        }
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDebugBorder(
        leftOffset: Float,
        topOffset: Float,
        metrics: BoardMetrics
    ) {
        drawRect(
            color = Color.Gray,
            topLeft = Offset(leftOffset, topOffset),
            size = androidx.compose.ui.geometry.Size(metrics.boardWidth, metrics.boardHeight),
            style = Stroke(width = 2f)
        )
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGuidanceLines(
        level: GameLevel,
        metrics: BoardMetrics,
        removalProgress: Map<Int, Float>,
        guidanceAlpha: Float,
        accentColor: Color,
        totalWidth: Float,
        totalHeight: Float,
        leftOffset: Float,
        topOffset: Float
    ) {
        level.snakes.forEach { snake ->
            if (removalProgress.containsKey(snake.id)) return@forEach

            val head = snake.body.first()
            val headCx = head.x * metrics.cellWidth + metrics.cellWidth / 2
            val headCy = head.y * metrics.cellHeight + metrics.cellHeight / 2

            val fullEndPoint = when (snake.headDirection) {
                Direction.UP -> Offset(headCx, -topOffset)
                Direction.DOWN -> Offset(headCx, metrics.boardHeight + (totalHeight - metrics.boardHeight - topOffset))
                Direction.LEFT -> Offset(-leftOffset, headCy)
                Direction.RIGHT -> Offset(metrics.boardWidth + (totalWidth - metrics.boardWidth - leftOffset), headCy)
            }

            val endPoint = Offset(
                x = headCx + (fullEndPoint.x - headCx) * guidanceAlpha,
                y = headCy + (fullEndPoint.y - headCy) * guidanceAlpha
            )

            drawLine(
                color = accentColor.copy(alpha = 0.4f * guidanceAlpha),
                start = Offset(headCx, headCy),
                end = endPoint,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(10f, 10f), 0f
                )
            )
        }
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDebugTapAreas(
        level: GameLevel,
        metrics: BoardMetrics
    ) {
        level.snakes.forEach { snake ->
            val head = snake.body.first()
            val headCx = head.x * metrics.cellWidth + metrics.cellWidth / 2
            val headCy = head.y * metrics.cellHeight + metrics.cellHeight / 2
            val tapRadius = DEFAULT_TOLERANCE * metrics.cellWidth

            val tapOffsetX = headCx + snake.headDirection.dx * metrics.cellWidth * TAP_AREA_OFFSET_FACTOR
            val tapOffsetY = headCy + snake.headDirection.dy * metrics.cellHeight * TAP_AREA_OFFSET_FACTOR

            drawCircle(
                color = LightGray.copy(alpha = 0.3f),
                radius = tapRadius,
                center = Offset(tapOffsetX, tapOffsetY)
            )
        }
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSnakes(
        level: GameLevel,
        metrics: BoardMetrics,
        removalProgress: Map<Int, Float>,
        flashingSnakeId: Int?,
        themeColors: com.batodev.arrows.ui.theme.ThemeColors
    ) {
        level.snakes.forEach { snake ->
            val p = (removalProgress[snake.id] ?: 0f).coerceIn(0f, 1f)
            val shift = metrics.moveDist * p
            val alpha = 1f - p

            val baseColor = if (snake.id == flashingSnakeId) FlashingRed else themeColors.snake
            val snakeColor = baseColor.copy(alpha = alpha)

            val head = snake.body.first()
            val headCx0 = head.x * metrics.cellWidth + metrics.cellWidth / 2
            val headCy0 = head.y * metrics.cellHeight + metrics.cellHeight / 2

            val headCx = headCx0 + snake.headDirection.dx * shift
            val headCy = headCy0 + snake.headDirection.dy * shift

            val lineEndX = headCx + snake.headDirection.dx * metrics.cornerRadius
            val lineEndY = headCy + snake.headDirection.dy * metrics.cornerRadius

            val baseLineEndX0 = headCx0 + snake.headDirection.dx * metrics.cornerRadius
            val baseLineEndY0 = headCy0 + snake.headDirection.dy * metrics.cornerRadius

            if (snake.body.size > 1) {
                drawSnakeBody(snake, p, metrics, headCx0, headCy0, baseLineEndX0, baseLineEndY0, lineEndX, lineEndY, snakeColor)
            }

            if (snake.body.size == 1) {
                drawSingleBlockSnakeTail(snake, metrics, lineEndX, lineEndY, snakeColor)
            }

            // Draw arrow head
            val triangleCenterX = lineEndX + snake.headDirection.dx * (metrics.arrowHeadSize * 0.5f)
            val triangleCenterY = lineEndY + snake.headDirection.dy * (metrics.arrowHeadSize * 0.5f)

            drawArrowHead(
                centerX = triangleCenterX,
                centerY = triangleCenterY,
                direction = snake.headDirection,
                arrowHeadSize = metrics.arrowHeadSize,
                color = snakeColor
            )
        }
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSnakeBody(
        snake: com.batodev.arrows.engine.Snake,
        p: Float,
        metrics: BoardMetrics,
        headCx0: Float,
        headCy0: Float,
        baseLineEndX0: Float,
        baseLineEndY0: Float,
        lineEndX: Float,
        lineEndY: Float,
        snakeColor: Color
    ) {
        val path = Path()
        val body = snake.body
        val segmentsToDraw = ((body.size - 1) * (1f - p)).toInt().coerceAtLeast(0)
        val lastSegmentIndex = (1 + segmentsToDraw).coerceAtMost(body.size - 1)

        val last = body[lastSegmentIndex]
        path.moveTo(
            last.x * metrics.cellWidth + metrics.cellWidth / 2,
            last.y * metrics.cellHeight + metrics.cellHeight / 2
        )

        for (i in lastSegmentIndex - 1 downTo 1) {
            val prev = body[i + 1]
            val current = body[i]
            val next = body[i - 1]

            val currX = current.x * metrics.cellWidth + metrics.cellWidth / 2
            val currY = current.y * metrics.cellHeight + metrics.cellHeight / 2

            val entryX = currX + (prev.x - current.x).coerceIn(-1, 1) * metrics.cornerRadius
            val entryY = currY + (prev.y - current.y).coerceIn(-1, 1) * metrics.cornerRadius

            val exitX = currX + (next.x - current.x).coerceIn(-1, 1) * metrics.cornerRadius
            val exitY = currY + (next.y - current.y).coerceIn(-1, 1) * metrics.cornerRadius

            path.lineTo(entryX, entryY)
            path.quadraticTo(currX, currY, exitX, exitY)
        }

        val head = body[0]
        val prev = body[1]
        val headEntryX = headCx0 + (prev.x - head.x).coerceIn(-1, 1) * metrics.cornerRadius
        val headEntryY = headCy0 + (prev.y - head.y).coerceIn(-1, 1) * metrics.cornerRadius

        path.lineTo(headEntryX, headEntryY)
        path.quadraticTo(headCx0, headCy0, baseLineEndX0, baseLineEndY0)

        if (p > 0f) {
            path.lineTo(lineEndX, lineEndY)
        }

        drawPath(
            path = path,
            color = snakeColor,
            style = Stroke(
                width = metrics.strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSingleBlockSnakeTail(
        snake: com.batodev.arrows.engine.Snake,
        metrics: BoardMetrics,
        lineEndX: Float,
        lineEndY: Float,
        snakeColor: Color
    ) {
        val tailLength = metrics.cellWidth * singleBlockTailFactor
        val tailStartX = lineEndX - snake.headDirection.dx * (tailLength + metrics.cornerRadius)
        val tailStartY = lineEndY - snake.headDirection.dy * (tailLength + metrics.cornerRadius)

        drawLine(
            color = snakeColor,
            start = Offset(tailStartX, tailStartY),
            end = Offset(lineEndX, lineEndY),
            strokeWidth = metrics.strokeWidth,
            cap = StrokeCap.Round
        )
    }

    private data class BoardMetrics(
        val cellWidth: Float,
        val cellHeight: Float,
        val strokeWidth: Float,
        val cornerRadius: Float,
        val arrowHeadSize: Float,
        val moveDist: Float,
        val boardWidth: Float,
        val boardHeight: Float
    )

    /**
     * Draws an equilateral triangle arrow head pointing in the specified direction.
     *
     * The arrow head is drawn as a filled triangle with a stroke outline. The triangle
     * has equal sides (120-degree angles) with rounded corners for a softer appearance.
     *
     * @param centerX X-coordinate of the arrow head center (inscribed circle center)
     * @param centerY Y-coordinate of the arrow head center (inscribed circle center)
     * @param direction The direction the arrow should point (UP, DOWN, LEFT, or RIGHT)
     * @param arrowHeadSize The radius of the inscribed circle (distance from center to vertices)
     * @param color The color to draw the arrow head (respects alpha for fade animations)
     */
    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowHead(
        centerX: Float,
        centerY: Float,
        direction: Direction,
        arrowHeadSize: Float,
        color: Color,
    ) {
        // Convert direction to angle in radians (0° = RIGHT, 90° = DOWN, etc.)
        val angle = when (direction) {
            Direction.UP -> 270.0
            Direction.DOWN -> 90.0
            Direction.LEFT -> 180.0
            Direction.RIGHT -> 0.0
        } * (PI / 180.0)

        // 120 degrees in radians for equilateral triangle
        val angleOffset = 2.094

        // Create triangular path with three vertices equally spaced around the center
        val path = Path().apply {
            // First vertex (points in the arrow direction)
            moveTo(
                centerX + (arrowHeadSize * cos(angle)).toFloat(),
                centerY + (arrowHeadSize * sin(angle)).toFloat()
            )
            // Second vertex (120 degrees clockwise)
            lineTo(
                centerX + (arrowHeadSize * cos(angle + angleOffset)).toFloat(),
                centerY + (arrowHeadSize * sin(angle + angleOffset)).toFloat()
            )
            // Third vertex (120 degrees counter-clockwise)
            lineTo(
                centerX + (arrowHeadSize * cos(angle - angleOffset)).toFloat(),
                centerY + (arrowHeadSize * sin(angle - angleOffset)).toFloat()
            )
            close()
        }

        // Draw filled triangle
        drawPath(path, color)

        // Draw outline stroke for better definition and rounded corners
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
