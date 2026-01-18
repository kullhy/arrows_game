package com.batodev.arrows

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.HeartRed
import com.batodev.arrows.ui.theme.ProgressBarGreen
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
        val application = applicationContext as ArrowsApplication
        setContent {
            val viewModel: AppViewModel = viewModel(
                factory = AppViewModel.Factory(application.userPreferencesRepository)
            )
            val currentTheme by viewModel.theme.collectAsState()

            ArrowsTheme(themeName = currentTheme) {
                val themeColors = com.batodev.arrows.ui.theme.LocalThemeColors.current
                Scaffold(
                    modifier = Modifier.fillMaxSize(), containerColor = themeColors.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ArrowsGameView(
                            repository = application.userPreferencesRepository
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArrowsGameView(
    repository: com.batodev.arrows.data.UserPreferencesRepository,
) {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val engine = remember {
        GameEngine(
            coroutineScope, repository, onVibrate = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            })
    }
    var state by remember { mutableStateOf<List<Party>>(emptyList()) }
    val tapAnimations =
        remember { androidx.compose.runtime.mutableStateListOf<TapAnimationState>() }
    val context = LocalContext.current

    val themeColors = com.batodev.arrows.ui.theme.LocalThemeColors.current

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
                    colors = IconButtonDefaults.iconButtonColors(containerColor = themeColors.topBarButton),
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
                    onClick = { engine.restartLevel() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = themeColors.topBarButton),
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
                repeat(3) { index ->
                    Icon(
                        imageVector = if (index < engine.lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Life",
                        tint = HeartRed,
                        modifier = Modifier.size(24.dp)
                    )
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }

            // Right Control (Loading/Hint)
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.topBarButton, contentColor = White
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
            trackColor = themeColors.topBarButton,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Game Area
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
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
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                engine.onTransform(pan, zoom)
                            }
                        }
                        .pointerInput(
                            engine.scale, engine.offsetX, engine.offsetY, engine.level
                        ) {
                            detectTapGestures { tapOffset ->
                                Log.v("TapDebug", "Container tap: $tapOffset, containerSize: ${size.width}")

                                // Pass raw tap offset and container size to engine
                                // Engine will handle all coordinate transformations
                                engine.onTap(tapOffset, size.width.toFloat(), 1000.dp.toPx())


                                // Store tap position in container coordinates for animation
                                tapAnimations.add(
                                    TapAnimationState(
                                        System.nanoTime(), tapOffset
                                    )
                                )
                            }
                        }
                ) {
                    Box(modifier = Modifier
                        .size(boardSize)
                        .graphicsLayer(
                            scaleX = engine.scale,
                            scaleY = engine.scale,
                            translationX = engine.offsetX,
                            translationY = engine.offsetY
                        )) {
                        ArrowsBoardRenderer.Board(
                            level = engine.level,
                            flashingSnakeId = engine.flashingSnakeId,
                            removalProgress = engine.removalProgress,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        )

                        // DEBUG: Draw marker at (0,0)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.Red,
                                radius = 20f,
                                center = Offset(0f, 0f)
                            )
                        }
                    }

                    // DEBUG: Draw marker at last tapOffset in container coordinates
                    if (tapAnimations.isNotEmpty()) {
                        val lastTap = tapAnimations.last().offset
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.Green,
                                radius = 20f,
                                center = lastTap
                            )
                        }
                    }

                    // Tap animations rendered in container coordinate space
                    tapAnimations.forEach { anim ->
                        key(anim.id) {
                            TapRipple(
                                offset = anim.offset,
                                onFinished = { tapAnimations.remove(anim) })
                        }
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
                        trackColor = themeColors.topBarButton
                    )
                }
            }

            if (state.isNotEmpty()) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = state,
                )
            }

            if (engine.lives <= 0) {
                GameOverDialog(
                    onRestart = { engine.restartLevel() },
                    onWatchAd = { engine.addLife() })
            }
        }
    }
}

@Composable
fun GameOverDialog(
    onRestart: () -> Unit,
    onWatchAd: () -> Unit,
) {
    val themeColors = com.batodev.arrows.ui.theme.LocalThemeColors.current
    AlertDialog(
        onDismissRequest = { /* Don't dismiss by clicking outside */ },
        containerColor = themeColors.bottomBar,
        title = {
            Text(
                text = "Game Over", color = White, fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "You are out of lives! Would you like to restart the board or watch an ad to get one more life?",
                color = White
            )
        },
        confirmButton = {
            Button(
                onClick = onWatchAd,
                colors = ButtonDefaults.buttonColors(containerColor = ProgressBarGreen)
            ) {
                Icon(
                    Icons.Default.VideoLabel,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Watch Ad", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onRestart) {
                Text("Restart Board", color = HeartRed)
            }
        })
}
