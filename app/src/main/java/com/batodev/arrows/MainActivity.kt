package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.batodev.arrows.engine.GameGenerator
import com.batodev.arrows.ui.theme.ArrowsTheme

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
    val engine = remember { GameGenerator() }
    val generateLevel = remember(engine) { { engine.generateSolvableLevel(15, 15, 12) } }
    var level by remember { mutableStateOf(generateLevel()) }

    // State for zoom and pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // State for selected snake
    var selectedSnakeId by remember { mutableStateOf<Int?>(null) }

    // Fixed board size - make it square and larger than typical screen
    val boardSize = 1000.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Board container with gesture support
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Keep the viewport square
                .padding(16.dp)
                .clipToBounds() // Clip content that extends beyond viewport
        ) {
            Box(
                modifier = Modifier
                    .size(boardSize) // Fixed square size
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.3f, 5f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
                    .pointerInput(scale, offsetX, offsetY, level) {
                        detectTapGestures { tapOffset ->
                            // Use actual element size from PointerInputScope
                            val boardSizePx = size.width.toFloat()

                            // Transform tap coordinates to content coordinates
                            // graphicsLayer applies: scale around center, then translate
                            // Inverse: subtract translation, then unscale around center
                            val center = boardSizePx / 2
                            val contentX = (tapOffset.x - offsetX - center) / scale + center
                            val contentY = (tapOffset.y - offsetY - center) / scale + center

                            // Convert to grid cell coordinates
                            val cellWidth = boardSizePx / level.width
                            val cellHeight = boardSizePx / level.height
                            val cellX = (contentX / cellWidth).toInt()
                            val cellY = (contentY / cellHeight).toInt()

                            // Check if tapped cell contains a snake head
                            val tappedSnake = level.snakes.find { snake ->
                                val head = snake.body.first()
                                head.x == cellX && head.y == cellY
                            }

                            selectedSnakeId = if (tappedSnake != null) {
                                if (selectedSnakeId == tappedSnake.id) null else tappedSnake.id
                            } else {
                                null // Deselect when tapping empty space
                            }
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            ) {
                ArrowsBoardRenderer.Board(
                    level = level,
                    selectedSnakeId = selectedSnakeId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = {
            level = generateLevel()
            // Reset zoom and pan on new level
            scale = 1f
            offsetX = 0f
            offsetY = 0f
            selectedSnakeId = null
        }) {
            androidx.compose.material3.Text("Regenerate Board")
        }
    }
}
