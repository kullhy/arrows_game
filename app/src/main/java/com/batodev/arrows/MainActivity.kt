package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.engine.GameLevel
import com.batodev.arrows.engine.Point
import com.batodev.arrows.ui.theme.ArrowsTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArrowsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White), // Clean white background like the image
                        contentAlignment = Alignment.Center
                    ) {
                        ArrowsGameView()
                    }
                }
            }
        }
    }
}

@Composable
fun ArrowsGameView() {
    val engine = remember { GameEngine() }
    val level = remember { engine.generateSolvableLevel(7, 17, 0.90) }

    // Board container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(7f / 11f)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / level.width
            val cellHeight = size.height / level.height
            val strokeWidth = cellWidth * 0.2f // Thick lines like the image

            level.snakes.forEach { snake ->
                val path = Path()

                // 1. Move to the center of the first segment (tail)
                val first = snake.body.first()
                path.moveTo(
                    first.x * cellWidth + cellWidth / 2,
                    first.y * cellHeight + cellHeight / 2
                )

                // 2. Draw lines through all segments
                for (i in 1 until snake.body.size) {
                    val p = snake.body[i]
                    path.lineTo(
                        p.x * cellWidth + cellWidth / 2,
                        p.y * cellHeight + cellHeight / 2
                    )
                }

                // 3. Draw the snake body
                drawPath(
                    path = path,
                    color = Color.Black,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 4. Draw the Arrow Head at the last segment
                val head = snake.body.last()
                drawArrowHead(
                    centerX = head.x * cellWidth + cellWidth / 2,
                    centerY = head.y * cellHeight + cellHeight / 2,
                    direction = snake.headDirection,
                    size = strokeWidth * 1.5f,
                    color = Color.Black
                )
            }
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

    val path = Path().apply {
        // Tip of the arrow
        moveTo(
            centerX + (size * cos(angle)).toFloat(),
            centerY + (size * sin(angle)).toFloat()
        )
        // Back left wing
        lineTo(
            centerX + (size * cos(angle + 2.5)).toFloat(),
            centerY + (size * sin(angle + 2.5)).toFloat()
        )
        // Back right wing
        lineTo(
            centerX + (size * cos(angle - 2.5)).toFloat(),
            centerY + (size * sin(angle - 2.5)).toFloat()
        )
        close()
    }
    drawPath(path, color)
}