package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.engine.GameLevel
import com.batodev.arrows.engine.Point
import com.batodev.arrows.ui.theme.ArrowsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArrowsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameBoardScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GameBoardScreen(modifier: Modifier = Modifier) {
    val engine = remember { GameEngine() }
    val level by remember {
        mutableStateOf(engine.generateSolvableLevel(width = 7, height = 10, fillDensity = 0.95))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Arrows Puzzle",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Board: ${level.width}×${level.height} | Snakes: ${level.snakes.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GameBoard(level = level)
    }
}

@Composable
fun GameBoard(level: GameLevel) {
    // Create a map from positions to snake info
    val grid = remember(level) {
        val g = Array(level.width) { IntArray(level.height) { 0 } }
        for (snake in level.snakes) {
            for (point in snake.body) {
                g[point.x][point.y] = snake.id
            }
        }
        g
    }

    val snakeMap = remember(level) {
        level.snakes.associateBy { it.id }
    }

    // Calculate cell size to fit the screen
    val cellSize = 40.dp

    Column(
        modifier = Modifier
            .border(2.dp, Color.Black)
            .padding(4.dp)
    ) {
        for (y in 0 until level.height) {
            Row {
                for (x in 0 until level.width) {
                    val snakeId = grid[x][y]
                    GameCell(
                        snakeId = snakeId,
                        point = Point(x, y),
                        snakeMap = snakeMap,
                        cellSize = cellSize
                    )
                }
            }
        }
    }
}

@Composable
fun GameCell(
    snakeId: Int,
    point: Point,
    snakeMap: Map<Int, com.batodev.arrows.engine.Snake>,
    cellSize: androidx.compose.ui.unit.Dp
) {
    val snake = snakeMap[snakeId]
    val isHead = snake?.body?.last() == point

    // Generate a color for each snake based on its ID
    val backgroundColor = if (snakeId == 0) {
        Color.White
    } else {
        val hue = (snakeId * 137.5f) % 360f // Golden angle for nice color distribution
        Color.hsv(hue, 0.5f, 0.95f)
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .border(0.5.dp, Color.LightGray)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            snakeId == 0 -> {
                // Empty cell - show a dot
                Text(
                    text = "·",
                    fontSize = 8.sp,
                    color = Color.LightGray
                )
            }
            isHead -> {
                // Head - show arrow
                val arrow = when (snake.headDirection) {
                    Direction.UP -> "↑"
                    Direction.DOWN -> "↓"
                    Direction.LEFT -> "←"
                    Direction.RIGHT -> "→"
                }
                Text(
                    text = arrow,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                // Body segment - show snake ID
                Text(
                    text = snakeId.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameBoardPreview() {
    ArrowsTheme {
        GameBoardScreen()
    }
}