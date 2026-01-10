package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
                            .background(Color.White),
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
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { GameEngine(coroutineScope) }

    val boardSize = 1000.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .size(boardSize)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            engine.onTransform(pan, zoom)
                        }
                    }
                    .pointerInput(engine.scale, engine.offsetX, engine.offsetY, engine.level) {
                        detectTapGestures { tapOffset ->
                            engine.onTap(tapOffset, size.width.toFloat())
                        }
                    }
                    .graphicsLayer(
                        scaleX = engine.scale,
                        scaleY = engine.scale,
                        translationX = engine.offsetX,
                        translationY = engine.offsetY
                    )
            ) {
                ArrowsBoardRenderer.Board(
                    level = engine.level,
                    flashingSnakeId = engine.flashingSnakeId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { engine.regenerateLevel() }) {
            Text("Regenerate Board")
        }
    }
}
