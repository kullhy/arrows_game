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
import androidx.compose.material3.LinearProgressIndicator
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class GameActivity : ComponentActivity() {
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
    var state by remember { mutableStateOf<List<Party>>(emptyList()) }

    if (engine.isGameWon && state.isEmpty()) {
        state = listOf(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                position = Position.Relative(0.5, 0.3),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
            )
        )
    } else if (!engine.isGameWon && state.isNotEmpty()) {
        state = emptyList()
    }

    val boardSize = 1000.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
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
                        .padding(8.dp)
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
                        removalProgress = engine.removalProgress,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { engine.regenerateLevel() },
                enabled = !engine.isLoading
            ) {
                Text("Regenerate Board")
            }
        }

        if (engine.isLoading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Generating... ${(engine.loadingProgress * 100).toInt()}%")
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { engine.loadingProgress },
                    modifier = Modifier.width(200.dp),
                )
            }
        }

        if (state.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = state,
            )
        }
    }
}