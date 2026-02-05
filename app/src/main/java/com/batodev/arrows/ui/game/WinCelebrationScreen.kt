package com.batodev.arrows.ui.game

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.batodev.arrows.GameConstants
import com.batodev.arrows.R
import kotlinx.coroutines.delay

@Composable
fun WinCelebrationScreen(onCelebrationComplete: () -> Unit) {
    var videoAlpha by remember { mutableStateOf(0f) }
    var shouldShowCelebration by remember { mutableStateOf(true) }
    var selectedLabel by remember { mutableStateOf(R.string.congratulations_super) }
    var selectedVideoResId by remember { mutableStateOf(R.raw.win1) }

    SelectRandomCelebrationContent { label, videoId ->
        selectedLabel = label
        selectedVideoResId = videoId
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = videoAlpha,
        animationSpec = tween(durationMillis = GameConstants.VIDEO_FADE_IN_DURATION),
        label = "video_fade"
    )

    PlayCelebrationTimeline(
        onFadeInStart = { videoAlpha = 1f },
        onFadeOutStart = { videoAlpha = 0f },
        onComplete = {
            shouldShowCelebration = false
            onCelebrationComplete()
        }
    )

    if (shouldShowCelebration) {
        CelebrationContent(selectedVideoResId, selectedLabel, animatedAlpha)
    }
}

@Composable
private fun SelectRandomCelebrationContent(
    onContentSelected: (Int, Int) -> Unit
) {
    LaunchedEffect(Unit) {
        val label = GameConstants.CONGRATULATION_LABELS.random()
        val videoId = GameConstants.WIN_VIDEOS.random()
        onContentSelected(label, videoId)
    }
}

@Composable
private fun PlayCelebrationTimeline(
    onFadeInStart: () -> Unit,
    onFadeOutStart: () -> Unit,
    onComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(GameConstants.VIDEO_PREPARATION_DELAY)
        onFadeInStart()
        delay(GameConstants.VIDEO_FADE_IN_DURATION.toLong())
        delay(GameConstants.VIDEO_DISPLAY_DURATION.toLong())
        onFadeOutStart()
        delay(GameConstants.VIDEO_FADE_OUT_DURATION.toLong())
        onComplete()
    }
}

@Composable
private fun CelebrationContent(
    videoResId: Int,
    labelResId: Int,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Video plays underneath
        VideoPlayerView(
            videoResId = videoResId,
            modifier = Modifier.fillMaxSize()
        )
        // Black overlay fades out (1-alpha) for fade-in, fades in for fade-out
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(1f - alpha)
                .background(Color.Black)
        )
        // Text fades with the content
        Text(
            text = stringResource(labelResId),
            fontSize = GameConstants.CONGRATULATIONS_FONT_SIZE.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.alpha(alpha)
        )
    }
}

@Composable
private fun VideoPlayerView(videoResId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val videoView = remember { VideoView(context) }

    DisposableEffect(videoResId) {
        val videoUri = Uri.parse("android.resource://${context.packageName}/$videoResId")
        videoView.setVideoURI(videoUri)
        videoView.start()

        onDispose {
            videoView.stopPlayback()
        }
    }

    AndroidView(
        factory = { videoView },
        modifier = modifier
    )
}
