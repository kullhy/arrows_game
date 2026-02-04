package com.batodev.arrows

import android.app.Activity
import android.os.Bundle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batodev.arrows.data.AndroidResourceBoardShapeProvider
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.engine.GameEngineConfig
import com.batodev.arrows.engine.GameEngineFeatures
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.ads.BannerAdView
import com.batodev.arrows.ui.game.GameProgressBar
import com.batodev.arrows.ui.game.GameTopBar
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.HeartRed
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ProgressBarGreen
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

private val CONFETTI_COLORS = listOf(
    GameConstants.CONFETTI_COLOR_1,
    GameConstants.CONFETTI_COLOR_2,
    GameConstants.CONFETTI_COLOR_3,
    GameConstants.CONFETTI_COLOR_4
)
private const val GAMES_BETWEEN_INTERSTITIALS = 5

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
                val repository = application.userPreferencesRepository
                val isAdFree by repository.isAdFree.collectAsState(initial = false)
                val themeColors = LocalThemeColors.current
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = themeColors.background
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        ArrowsGameView(repository, isAdFree)
                    }
                }
            }
        }
    }
}

@Composable
fun ArrowsGameView(
    repository: com.batodev.arrows.data.UserPreferencesRepository,
    isAdFree: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    val activity = context as? Activity
    val application = context.applicationContext as ArrowsApplication
    val customParams = extractCustomGameParams(activity?.intent)

    val engine = remember {
        GameEngine(
            config = GameEngineConfig(
                coroutineScope = coroutineScope, repository = repository,
                isCustomGame = customParams.isCustom
            ),
            features = GameEngineFeatures(
                onVibrate = { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) },
                soundManager = SoundManager(context),
                shapeProvider = AndroidResourceBoardShapeProvider(context),
                forcedWidth = customParams.customWidth,
                forcedHeight = customParams.customHeight,
                forcedShape = customParams.customShape
            )
        )
    }

    var confettiState by remember { mutableStateOf<List<Party>>(emptyList()) }
    var showGuidanceLines by remember { mutableStateOf(false) }
    val guidanceAlpha by animateFloatAsState(
        targetValue = if (showGuidanceLines) 1f else 0f,
        animationSpec = tween(durationMillis = GameConstants.GUIDANCE_ANIM_DURATION),
        label = stringResource(R.string.content_description_guidance_lines)
    )
    val tapAnimations = remember { androidx.compose.runtime.mutableStateListOf<TapAnimationState>() }
    val themeColors = LocalThemeColors.current

    HandleGameWonState(
        GameWonStateParams(engine, repository, activity, application, isAdFree)
    )
    confettiState = updateConfettiState(engine, confettiState)

    Column(modifier = Modifier.fillMaxSize()) {
        GameTopBar(
            lives = engine.lives,
            maxLives = engine.maxLives,
            onRestart = { engine.restartLevel() },
            onHint = { engine.showHint() },
            onBack = { (context as? Activity)?.finish() }
        )

        GameProgressBar(
            totalSnakes = engine.totalSnakesInLevel,
            currentSnakes = engine.level.snakes.size
        )

        GameArea(GameAreaParams(engine, tapAnimations, guidanceAlpha, showGuidanceLines, themeColors) {
            showGuidanceLines = !showGuidanceLines
        })

        if (!isAdFree) {
            BannerAdView()
        }
    }
}

@Composable
private fun HandleGameWonState(params: GameWonStateParams) {
    LaunchedEffect(params.engine.isGameWon) {
        if (params.engine.isGameWon) {
            // Increment games completed
            params.repository.incrementGamesCompleted()

            // Show interstitial ad every 5 games (if not ad-free)
            val gamesCompleted = params.repository.gamesCompleted.first()
            if (!params.isAdFree && gamesCompleted % GAMES_BETWEEN_INTERSTITIALS == 0) {
                params.activity?.let { act ->
                    params.application.interstitialAdManager.showInterstitialAd(act) {
                        params.activity.finish()
                    }
                } ?: run {
                    delay(GameConstants.GAME_WON_EXIT_DELAY)
                    params.activity?.finish()
                }
            } else {
                delay(GameConstants.GAME_WON_EXIT_DELAY)
                params.activity?.finish()
            }
        }
    }
}

@Composable
private fun updateConfettiState(engine: GameEngine, currentState: List<Party>): List<Party> {
    return if (engine.isGameWon && currentState.isEmpty()) {
        listOf(
            Party(
                speed = 0f, maxSpeed = GameConstants.CONFETTI_MAX_SPEED, damping = GameConstants.CONFETTI_DAMPING,
                spread = GameConstants.CONFETTI_SPREAD,
                colors = CONFETTI_COLORS,
                position = Position.Relative(GameConstants.CONFETTI_REL_X, GameConstants.CONFETTI_REL_Y),
                emitter = Emitter(
                    duration = GameConstants.CONFETTI_DURATION_MS, TimeUnit.MILLISECONDS
                ).max(GameConstants.CONFETTI_EMITTER_MAX)
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
                parties = updateConfettiState(params.engine, emptyList())
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
            modifier = Modifier.fillMaxSize()
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
        Icon(
            imageVector = Icons.Default.Grid4x4,
            contentDescription = stringResource(R.string.content_description_guidance_lines),
            tint = White
        )
    }
}

@Composable
private fun DebugOverlay(tapAnimations: SnapshotStateList<TapAnimationState>) {
    if (tapAnimations.isNotEmpty()) {
        val lastTap = tapAnimations.last().offset
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.Green, radius = GameConstants.DEBUG_CIRCLE_RADIUS, center = lastTap)
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
        val progressPercent = (progress * GameConstants.PERCENT_MULTIPLIER).toInt()
        Text(stringResource(R.string.generating_progress, progressPercent), color = White)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(GameConstants.PROGRESS_BAR_WIDTH.dp),
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
        title = { Text(text = stringResource(R.string.game_over_title), color = White, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = stringResource(R.string.game_over_message),
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
                Text(stringResource(R.string.watch_ad_label), color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onRestart) {
                Text(stringResource(R.string.restart_board_label), color = HeartRed)
            }
        }
    )
}
