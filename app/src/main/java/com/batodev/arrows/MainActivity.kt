package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.batodev.arrows.engine.GameEngine
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
    val engine = remember { GameEngine() }
    var level by remember { mutableStateOf(engine.generateSolvableLevel(7, 17, 1.0)) }

    // State for zoom and pan
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

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
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            ) {
                ArrowsBoardRenderer.Board(level = level, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = {
            level = engine.generateSolvableLevel(7, 17, 1.0)
            // Reset zoom and pan on new level
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }) {
            androidx.compose.material3.Text("Regenerate Board")
        }
    }
}
