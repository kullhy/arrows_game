package com.batodev.arrows

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
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
import com.batodev.arrows.ads.InterstitialAdManager
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.feature.game.BuildConfig
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.UserPreferencesRepository
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.ads.BannerAdView
import com.batodev.arrows.ui.game.GameProgressBar
import com.batodev.arrows.ui.game.GameTopBar
import com.batodev.arrows.ui.game.GameTopBarCallbacks
import com.batodev.arrows.ui.game.GameTopBarState
import com.batodev.arrows.ui.game.HintButtonState
import com.batodev.arrows.ui.game.IntroFingerOverlay
import com.batodev.arrows.ui.game.WinCelebrationScreen
import com.batodev.arrows.ui.game.rememberIntroState
import com.batodev.arrows.ui.theme.HeartRed
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ProgressBarGreen
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

data class CelebrationParams(
    val showCelebration: Boolean,
    val onCelebrationComplete: () -> Unit
)

private val LocalCelebrationParams = compositionLocalOf<CelebrationParams> {
    error("CelebrationParams not provided")
}

private val CONFETTI_COLORS = listOf(
    GameConstants.CONFETTI_COLOR_1,
    GameConstants.CONFETTI_COLOR_2,
    GameConstants.CONFETTI_COLOR_3,
    GameConstants.CONFETTI_COLOR_4
)

@Composable
fun ArrowsGameView(
    appViewModel: AppViewModel,
    isAdFree: Boolean,
    rewardAdManager: RewardAdManager,
    interstitialAdManager: InterstitialAdManager,
    userPreferencesRepository: UserPreferencesRepository,
    gameStateDao: GameStateDao,
    customParams: CustomGameParams = CustomGameParams(false, null, null, null),
    onBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    val activity = context as? Activity
    val isAdLoaded by rewardAdManager.isAdLoaded.collectAsState()
    val isAdLoading by rewardAdManager.isAdLoading.collectAsState()
    val engine: GameEngine = viewModel(
        factory = createGameEngineFactory(view, context, userPreferencesRepository, gameStateDao, customParams)
    )
    val introState = rememberIntroState(appViewModel, engine.isLoading, engine.level.snakes.size)
    val isWinVideosEnabled by appViewModel.isWinVideosEnabled.collectAsState()
    var confettiState by remember { mutableStateOf<List<Party>>(emptyList()) }
    var showGuidanceLines by remember { mutableStateOf(false) }
    var showCelebrationVideo by remember { mutableStateOf(false) }
    val guidanceAlpha by animateFloatAsState(
        targetValue = if (showGuidanceLines) 1f else 0f,
        animationSpec = tween(durationMillis = GameConstants.GUIDANCE_ANIM_DURATION),
        label = stringResource(R.string.content_description_guidance_lines)
    )
    val tapAnimations = remember { androidx.compose.runtime.mutableStateListOf<TapAnimationState>() }
    val themeColors = LocalThemeColors.current
    val gameWonParams = remember(activity, interstitialAdManager, isAdFree) {
        GameWonStateParams(engine, appViewModel, activity ?: return@remember null, interstitialAdManager, isAdFree, onFinish = onBack)
    }
    if (gameWonParams != null) {
        HandleGameWonState(gameWonParams, isWinVideosEnabled) { showCelebrationVideo = true }
    }
    confettiState = updateConfettiState(engine, confettiState)
    val handleHint = buildHintHandler(
        HintHandlerParams(isAdFree, isAdLoading, isAdLoaded, engine, activity, rewardAdManager)
    )
    val onCelebrationComplete: () -> Unit = {
        coroutineScope.launch {
            if (gameWonParams != null) {
                finishGameAfterCelebration(gameWonParams, waitForConfetti = false)
            }
        }
    }
    val celebrationParams = CelebrationParams(
        showCelebration = showCelebrationVideo && engine.isGameWon,
        onCelebrationComplete = onCelebrationComplete
    )
    CompositionLocalProvider(LocalCelebrationParams provides celebrationParams) {
        GameScreenContent(
            GameScreenContentParams(
                engine, activity, context, tapAnimations, guidanceAlpha, showGuidanceLines, themeColors,
                rewardAdManager, isAdFree, isAdLoaded, isAdLoading, handleHint,
                { showGuidanceLines = !showGuidanceLines }, showCelebrationVideo, onCelebrationComplete,
                introState.showIntro, introState.onDismiss, onBack
            )
        )
    }
}

@Composable
private fun HandleGameWonState(
    params: GameWonStateParams,
    isWinVideosEnabled: Boolean,
    onShowCelebration: () -> Unit
) {
    LaunchedEffect(params.engine.isGameWon) {
        if (params.engine.isGameWon) {
            if (isWinVideosEnabled) {
                onShowCelebration()
            } else {
                finishGameAfterCelebration(params, waitForConfetti = true)
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
        ResetViewButton(params.themeColors) { params.engine.transformationState.reset() }
        GuidanceToggleButton(params.showGuidanceLines, params.themeColors, params.onToggleGuidance)
        if (BuildConfig.DRAW_DEBUG_STUFF) DebugOverlay(params.tapAnimations)
        TapAnimationsLayer(params.tapAnimations)
        if (params.engine.isLoading) LoadingOverlay(params.engine.loadingProgress, params.themeColors)
        val celebrationParams = LocalCelebrationParams.current
        if (celebrationParams.showCelebration) {
            WinCelebrationScreen(onCelebrationComplete = celebrationParams.onCelebrationComplete)
        }
        if (params.engine.isGameWon) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = updateConfettiState(params.engine, emptyList())
            )
        }
        if (params.showIntro) {
            IntroFingerOverlay(level = params.engine.level)
        }
        if (params.engine.lives <= 0) {
            GameOverDialog(
                rewardAdManager = params.rewardAdManager,
                activity = params.activity,
                isAdFree = params.isAdFree,
                onRestart = { params.engine.restartLevel() },
                onWatchAd = { params.engine.addLife() }
            )
        }
    }
}

@Composable
private fun GameScreenContent(params: GameScreenContentParams) {
    Column(modifier = Modifier.fillMaxSize()) {
        GameTopBar(
            state = GameTopBarState(
                lives = params.engine.lives,
                maxLives = params.engine.maxLives,
                hintState = HintButtonState(params.isAdFree, params.isAdLoaded, params.isAdLoading)
            ),
            callbacks = GameTopBarCallbacks(
                onRestart = { params.engine.restartLevel() },
                onHint = params.handleHint,
                onBack = params.onBack
            )
        )
        GameProgressBar(
            totalSnakes = params.engine.totalSnakesInLevel,
            currentSnakes = params.engine.level.snakes.size
        )
        GameArea(
            GameAreaParams(
                params.engine, params.tapAnimations, params.guidanceAlpha, params.showGuidanceLines,
                params.themeColors, params.rewardAdManager, params.activity, params.isAdFree,
                params.onToggleGuidance, params.showIntro, params.onDismissIntro
            )
        )
        if (!params.isAdFree) {
            BannerAdView()
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
    rewardAdManager: RewardAdManager,
    activity: Activity?,
    isAdFree: Boolean,
    onRestart: () -> Unit,
    onWatchAd: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val isAdLoaded by rewardAdManager.isAdLoaded.collectAsState()
    val isAdLoading by rewardAdManager.isAdLoading.collectAsState()

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
                onClick = {
                    if (isAdFree) {
                        onWatchAd()
                    } else {
                        activity?.let { act ->
                            rewardAdManager.showRewardAd(
                                activity = act,
                                onRewarded = onWatchAd,
                                onAdDismissed = { /* No action needed */ }
                            )
                        }
                    }
                },
                enabled = isAdFree || (isAdLoaded && !isAdLoading),
                colors = ButtonDefaults.buttonColors(containerColor = ProgressBarGreen)
            ) {
                Icon(
                    Icons.Default.VideoLabel,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAdFree) {
                        stringResource(R.string.add_life_label)
                    } else if (isAdLoading) {
                        stringResource(R.string.loading_ad)
                    } else if (!isAdLoaded) {
                        stringResource(R.string.ad_not_ready)
                    } else {
                        stringResource(R.string.watch_ad_label)
                    },
                    color = White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onRestart) {
                Text(stringResource(R.string.restart_board_label), color = HeartRed)
            }
        }
    )
}
