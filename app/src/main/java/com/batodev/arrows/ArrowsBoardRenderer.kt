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
    ) {
        val themeColors = com.batodev.arrows.ui.theme.LocalThemeColors.current
        val debug = BuildConfig.DEBUG || false
        Canvas(modifier = modifier) {
            val totalDrawTime = measureTimeMillis {
                // Calculate cell dimensions based on canvas size and grid dimensions
                val cellWidth = size.width / level.width
                val cellHeight = size.height / level.height

                // Visual styling parameters
                val strokeWidth = cellWidth * 0.15f // Snake body thickness
                val cornerRadius = cellWidth * 0.3f // Radius for rounded corners at turns
                val arrowHeadSize = cellWidth * ARROW_HEAD_SIZE_FACTOR

                // Distance to move snake head during removal animation (off-screen)
                val moveDist = max(size.width, size.height) * 1.2f

                // Draw game area border in debug builds
                if (debug) {
                    drawRect(
                        color = Color.Gray,
                        size = size,
                        style = Stroke(width = 2f)
                    )
                }

                // Draw tap areas for snake heads (debug visualization only)
                if (debug) {
                    val tapTolerance = 0.6f // Tap radius in cells
                    level.snakes.forEach { snake ->
                        val head = snake.body.first()
                        // Calculate center of head cell
                        val headCx = head.x * cellWidth + cellWidth / 2
                        val headCy = head.y * cellHeight + cellHeight / 2
                        val tapRadius = tapTolerance * cellWidth

                        // Shift tap area in arrow direction for easier tapping
                        val tapOffsetX =
                            headCx + snake.headDirection.dx * cellWidth * TAP_AREA_OFFSET_FACTOR
                        val tapOffsetY =
                            headCy + snake.headDirection.dy * cellHeight * TAP_AREA_OFFSET_FACTOR

                        // Draw semi-transparent circle showing tappable area
                        drawCircle(
                            color = LightGray.copy(alpha = 0.3f),
                            radius = tapRadius,
                            center = Offset(tapOffsetX, tapOffsetY)
                        )
                    }
                }

                level.snakes.forEach { snake ->
                    // Get removal animation progress (0.0 = not started, 1.0 = fully removed)
                    val p = (removalProgress[snake.id] ?: 0f).coerceIn(0f, 1f)
                    val shift = moveDist * p // How far the head has moved
                    val alpha = 1f - p // Fade out as removal progresses

                    val path = Path()
                    val body = snake.body
                    // Use FlashingRed if snake is flashing (obstructed), otherwise themeColors.snake
                    val baseColor =
                        if (snake.id == flashingSnakeId) FlashingRed else themeColors.snake
                    val snakeColor = baseColor.copy(alpha = alpha)

                    val head = body.first()
                    // Original head position (center of cell)
                    val headCx0 = head.x * cellWidth + cellWidth / 2
                    val headCy0 = head.y * cellHeight + cellHeight / 2

                    // Head moves forward during removal animation
                    val headCx = headCx0 + snake.headDirection.dx * shift
                    val headCy = headCy0 + snake.headDirection.dy * shift

                    // Arrow base (also moves with head during animation)
                    val lineEndX = headCx + snake.headDirection.dx * cornerRadius
                    val lineEndY = headCy + snake.headDirection.dy * cornerRadius

                    // Original (non-animated) arrow base position
                    // We draw the normal curved approach into this, then extend with
                    // a straight segment to the moved arrow base
                    val baseLineEndX0 = headCx0 + snake.headDirection.dx * cornerRadius
                    val baseLineEndY0 = headCy0 + snake.headDirection.dy * cornerRadius

                    if (body.size > 1) {
                        // Calculate how many body segments to draw (shrink tail during removal)
                        // When p=0: draw all segments (lastSegmentIndex = body.size - 1)
                        // When p=1: draw minimum segments (lastSegmentIndex = 1)
                        val segmentsToDraw = ((body.size - 1) * (1f - p)).toInt().coerceAtLeast(0)
                        val lastSegmentIndex = (1 + segmentsToDraw).coerceAtMost(body.size - 1)

                        // Start path from the tail (last segment to be drawn)
                        val last = body[lastSegmentIndex]
                        path.moveTo(
                            last.x * cellWidth + cellWidth / 2,
                            last.y * cellHeight + cellHeight / 2
                        )

                        // Draw each segment from tail towards head
                        for (i in lastSegmentIndex - 1 downTo 1) {
                            val prev = body[i + 1] // Previous segment in path (closer to tail)
                            val current = body[i]  // Current segment
                            val next = body[i - 1] // Next segment in path (closer to head)

                            // Calculate center of current cell
                            val currX = current.x * cellWidth + cellWidth / 2
                            val currY = current.y * cellHeight + cellHeight / 2

                            // Entry point: where line enters the current cell from previous segment
                            val entryX = currX + (prev.x - current.x).coerceIn(-1, 1) * cornerRadius
                            val entryY = currY + (prev.y - current.y).coerceIn(-1, 1) * cornerRadius

                            // Exit point: where line exits the current cell towards next segment
                            val exitX = currX + (next.x - current.x).coerceIn(-1, 1) * cornerRadius
                            val exitY = currY + (next.y - current.y).coerceIn(-1, 1) * cornerRadius

                            // Draw straight line to entry, then curved corner through the cell center
                            path.lineTo(entryX, entryY)
                            path.quadraticTo(currX, currY, exitX, exitY)
                        }

                        // Connect last body segment to the head with a curved corner
                        val prev = body[1]
                        val headEntryX = headCx0 + (prev.x - head.x).coerceIn(-1, 1) * cornerRadius
                        val headEntryY = headCy0 + (prev.y - head.y).coerceIn(-1, 1) * cornerRadius

                        path.lineTo(headEntryX, headEntryY)

                        // Draw curved approach into the head cell/arrow base (non-animated position)
                        path.quadraticTo(headCx0, headCy0, baseLineEndX0, baseLineEndY0)

                        // During removal animation, extend with a straight "link" line to the animated arrow base
                        if (p > 0f) {
                            path.lineTo(lineEndX, lineEndY)
                        }

                        // Draw the complete snake body path
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

                    // For single-cell snakes, draw a simple tail line
                    if (body.size == 1) {
                        val tailLength = cellWidth * singleBlockTailFactor
                        val tailStartX =
                            lineEndX - snake.headDirection.dx * (tailLength + cornerRadius)
                        val tailStartY =
                            lineEndY - snake.headDirection.dy * (tailLength + cornerRadius)

                        drawLine(
                            color = snakeColor,
                            start = Offset(tailStartX, tailStartY),
                            end = Offset(lineEndX, lineEndY),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw arrow head at the front of the snake
                    // Position it slightly ahead of the arrow base for proper appearance
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
            }
            // Log the total time taken to draw the board for performance monitoring
            Log.v(
                ArrowsBoardRenderer.javaClass.simpleName,
                "Total board draw time: $totalDrawTime ms"
            )
        }
    }

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
