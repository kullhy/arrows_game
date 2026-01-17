package com.batodev.arrows

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.DarkBackground
import com.batodev.arrows.ui.theme.HeartRed
import com.batodev.arrows.ui.theme.ProgressBarGreen
import com.batodev.arrows.ui.theme.TopBarButtonBackground
import com.batodev.arrows.ui.theme.White
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
                Scaffold(modifier = Modifier.fillMaxSize(), containerColor = DarkBackground) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
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
    val context = LocalContext.current

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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { (context as? Activity)?.finish() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = TopBarButtonBackground),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { engine.regenerateLevel() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = TopBarButtonBackground),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart",
                        tint = White
                    )
                }
            }

            // Hearts
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Life",
                    tint = HeartRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Life",
                    tint = HeartRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Life",
                    tint = HeartRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Right Control (Loading/Hint)
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TopBarButtonBackground,
                    contentColor = White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLabel,
                    contentDescription = "Ad",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Loading..", fontSize = 12.sp)
            }
        }

        // Progress Bar
        val targetProgress = if (engine.totalSnakesInLevel > 0) {
            (engine.totalSnakesInLevel - engine.level.snakes.size).toFloat() / engine.totalSnakesInLevel
        } else 0f

        val animatedProgress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 500),
            label = "ProgressBarAnimation"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = ProgressBarGreen,
            trackColor = TopBarButtonBackground,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Game Area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
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
            }

            if (engine.isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Generating... ${(engine.loadingProgress * 100).toInt()}%", color = White)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { engine.loadingProgress },
                        modifier = Modifier.width(200.dp),
                        color = ProgressBarGreen,
                        trackColor = TopBarButtonBackground
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
}
