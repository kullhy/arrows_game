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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grid4x4
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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batodev.arrows.data.AndroidResourceBoardShapeProvider
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.engine.GameEngineConfig
import com.batodev.arrows.engine.GameEngineFeatures
import com.batodev.arrows.engine.TapParams
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.game.GameProgressBar
import com.batodev.arrows.ui.game.GameTopBar
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.HeartRed
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ProgressBarGreen
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

private const val GAME_WON_EXIT_DELAY = 3000L
private const val GUIDANCE_ANIM_DURATION = 500
private const val PROGRESS_BAR_WIDTH = 200
private const val DEBUG_CIRCLE_RADIUS = 20f
private const val PERCENT_MULTIPLIER = 100

private const val CONFETTI_MAX_SPEED = 30f
private const val CONFETTI_DAMPING = 0.9f
private const val CONFETTI_SPREAD = 360
private const val CONFETTI_DURATION_MS = 100L
private const val CONFETTI_EMITTER_MAX = 100
private const val CONFETTI_REL_X = 0.5
private const val CONFETTI_REL_Y = 0.3

private const val CONFETTI_COLOR_1 = 0xfce18a
private const val CONFETTI_COLOR_2 = 0xff726d
private const val CONFETTI_COLOR_3 = 0xf4306d
private const val CONFETTI_COLOR_4 = 0xb48def

private val CONFETTI_COLORS = listOf(CONFETTI_COLOR_1, CONFETTI_COLOR_2, CONFETTI_COLOR_3, CONFETTI_COLOR_4)

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
                val themeColors = LocalThemeColors.current
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = themeColors.background
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        ArrowsGameView(application.userPreferencesRepository)
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
    val engine = remember {
        GameEngine(
            config = GameEngineConfig(coroutineScope = coroutineScope, repository = repository),
            features = GameEngineFeatures(
                onVibrate = { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) },
                soundManager = SoundManager(context),
                shapeProvider = AndroidResourceBoardShapeProvider(context)
            )
        )
    }

    var confettiState by remember { mutableStateOf<List<Party>>(emptyList()) }
    var showGuidanceLines by remember { mutableStateOf(false) }
    val guidanceAlpha by animateFloatAsState(
        targetValue = if (showGuidanceLines) 1f else 0f,
        animationSpec = tween(durationMillis = GUIDANCE_ANIM_DURATION),
        label = "GuidanceAlphaAnimation"
    )
    val tapAnimations = remember { androidx.compose.runtime.mutableStateListOf<TapAnimationState>() }
    val themeColors = LocalThemeColors.current

    HandleGameWonState(engine, context)
    confettiState = UpdateConfettiState(engine, confettiState)

    Column(modifier = Modifier.fillMaxSize()) {
        GameTopBar(
            lives = engine.lives,
            maxLives = engine.maxLives,
            onRestart = { engine.restartLevel() },
            onBack = { (context as? Activity)?.finish() }
        )

        GameProgressBar(
            totalSnakes = engine.totalSnakesInLevel,
            currentSnakes = engine.level.snakes.size
        )

        GameArea(GameAreaParams(engine, tapAnimations, guidanceAlpha, showGuidanceLines, themeColors) {
            showGuidanceLines = !showGuidanceLines
        })
    }
}

@Composable
private fun HandleGameWonState(engine: GameEngine, context: android.content.Context) {
    LaunchedEffect(engine.isGameWon) {
        if (engine.isGameWon) {
            delay(GAME_WON_EXIT_DELAY)
            (context as? android.app.Activity)?.finish()
        }
    }
}

@Composable
private fun UpdateConfettiState(engine: GameEngine, currentState: List<Party>): List<Party> {
    return if (engine.isGameWon && currentState.isEmpty()) {
        listOf(
            Party(
                speed = 0f, maxSpeed = CONFETTI_MAX_SPEED, damping = CONFETTI_DAMPING, 
                spread = CONFETTI_SPREAD,
                colors = CONFETTI_COLORS,
                position = Position.Relative(CONFETTI_REL_X, CONFETTI_REL_Y),
                emitter = Emitter(
                    duration = CONFETTI_DURATION_MS, TimeUnit.MILLISECONDS
                ).max(CONFETTI_EMITTER_MAX)
            )
        )
    } else if (!engine.isGameWon && currentState.isNotEmpty()) {
        emptyList()
    } else {
        currentState
    }
}

@Composable
private fun ColumnScope.GameArea(params: GameAreaParams) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .padding(16.dp)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ -> params.engine.onTransform(pan, zoom) }
            }
            .pointerInput(params.engine.scale, params.engine.offsetX, params.engine.offsetY, params.engine.level) {
                detectTapGestures { tapOffset ->
                    params.engine.onTap(tapOffset, size.width.toFloat(), size.height.toFloat())
                    params.tapAnimations.add(TapAnimationState(System.nanoTime(), tapOffset))
                }
            }
    ) {
        BoardLayer(params.engine, params.guidanceAlpha)
        GuidanceToggleButton(params.showGuidanceLines, params.themeColors, params.onToggleGuidance)
        if (BuildConfig.DRAW_DEBUG_STUFF) DebugOverlay(params.tapAnimations)
        TapAnimationsLayer(params.tapAnimations)
        if (params.engine.isLoading) LoadingOverlay(params.engine.loadingProgress, params.themeColors)
        if (params.engine.isGameWon) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(), 
                parties = UpdateConfettiState(params.engine, emptyList())
            )
        }
        if (params.engine.lives <= 0) {
            GameOverDialog(
                onRestart = { params.engine.restartLevel() }, 
                onWatchAd = { params.engine.addLife() }
            )
        }
    }
}

@Composable
private fun BoardLayer(engine: GameEngine, guidanceAlpha: Float) {
    Box(modifier = Modifier
        .fillMaxSize()
        .graphicsLayer(
            scaleX = engine.scale, scaleY = engine.scale,
            translationX = engine.offsetX, translationY = engine.offsetY
        )) {
        ArrowsBoardRenderer.Board(
            level = engine.level,
            flashingSnakeId = engine.flashingSnakeId,
            removalProgress = engine.removalProgress,
            guidanceAlpha = guidanceAlpha,
            modifier = Modifier.fillMaxSize().padding(10.dp)
        )
    }
}

@Composable
private fun BoxScope.GuidanceToggleButton(
    showGuidanceLines: Boolean,
    themeColors: ThemeColors,
    onToggleGuidance: () -> Unit
) {
    IconButton(
        onClick = onToggleGuidance,
        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (showGuidanceLines) themeColors.accent else themeColors.topBarButton,
            contentColor = White
        )
    ) {
        Icon(imageVector = Icons.Default.Grid4x4, contentDescription = "Guidance Lines", tint = White)
    }
}

@Composable
private fun DebugOverlay(tapAnimations: SnapshotStateList<TapAnimationState>) {
    if (tapAnimations.isNotEmpty()) {
        val lastTap = tapAnimations.last().offset
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.Green, radius = DEBUG_CIRCLE_RADIUS, center = lastTap)
        }
    }
}

@Composable
private fun TapAnimationsLayer(tapAnimations: SnapshotStateList<TapAnimationState>) {
    tapAnimations.forEach { anim ->
        key(anim.id) {
            TapRipple(offset = anim.offset, onFinished = { tapAnimations.remove(anim) })
        }
    }
}

@Composable
private fun BoxScope.LoadingOverlay(progress: Float, themeColors: ThemeColors) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Generating... ${(progress * PERCENT_MULTIPLIER).toInt()}%", color = White)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(PROGRESS_BAR_WIDTH.dp),
            color = ProgressBarGreen,
            trackColor = themeColors.topBarButton
        )
    }
}

@Composable
fun GameOverDialog(
    onRestart: () -> Unit,
    onWatchAd: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = { },
        containerColor = themeColors.bottomBar,
        title = { Text(text = "Game Over", color = White, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "You are out of lives! Would you like to restart the board or watch an ad?",
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
        }
    )
}
