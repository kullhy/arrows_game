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
import androidx.compose.material.icons.filled.Grid4x4
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
import androidx.compose.runtime.LaunchedEffect
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
import com.batodev.arrows.ui.game.GameProgressBar
import com.batodev.arrows.ui.game.GameTopBar
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.HeartRed
import com.batodev.arrows.ui.theme.ProgressBarGreen
import com.batodev.arrows.ui.theme.White
import kotlinx.coroutines.delay
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
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val engine = remember {
        GameEngine(
            coroutineScope, repository, onVibrate = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }, soundManager = soundManager)
    }
    var state by remember { mutableStateOf<List<Party>>(emptyList()) }
    var showGuidanceLines by remember { mutableStateOf(false) }
    val guidanceAlpha by animateFloatAsState(
        targetValue = if (showGuidanceLines) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "GuidanceAlphaAnimation"
    )
    val tapAnimations =
        remember { androidx.compose.runtime.mutableStateListOf<TapAnimationState>() }

    val themeColors = com.batodev.arrows.ui.theme.LocalThemeColors.current

    LaunchedEffect(engine.isGameWon) {
        if (engine.isGameWon) {
            delay(3000) // Wait for confetti
            (context as? Activity)?.finish()
        }
    }

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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        GameTopBar(
            lives = engine.lives,
            maxLives = engine.maxLives,
            onRestart = { engine.restartLevel() },
            onBack = { (context as? Activity)?.finish() }
        )

        // Progress Bar
        GameProgressBar(
            totalSnakes = engine.totalSnakesInLevel,
            currentSnakes = engine.level.snakes.size
        )

        // Game Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
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
                        if (BuildConfig.DRAW_DEBUG_STUFF) {
                            Log.v(
                                "TapDebug",
                                "Container tap: $tapOffset, containerSize: ${size.width}x${size.height}"
                            )
                        }

                        // Pass raw tap offset and container size to engine
                        // Engine will handle all coordinate transformations
                        engine.onTap(
                            tapOffset,
                            size.width.toFloat(),
                            size.height.toFloat()
                        )


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
                .fillMaxSize()
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
                    guidanceAlpha = guidanceAlpha,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )

                if (BuildConfig.DRAW_DEBUG_STUFF) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.Red,
                            radius = 20f,
                            center = Offset(0f, 0f)
                        )
                    }
                }
            }

            // Guidance Lines Toggle Button
            IconButton(
                onClick = { showGuidanceLines = !showGuidanceLines },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (showGuidanceLines) themeColors.accent else themeColors.topBarButton,
                    contentColor = White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Grid4x4,
                    contentDescription = "Guidance Lines",
                    tint = White
                )
            }

            // DEBUG: Draw marker at last tapOffset in container coordinates
            if (BuildConfig.DRAW_DEBUG_STUFF && tapAnimations.isNotEmpty()) {
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

            if (engine.isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
