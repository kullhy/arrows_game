package com.batodev.arrows

import android.app.Activity
import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.data.AndroidResourceBoardShapeProvider
import com.batodev.arrows.data.UserPreferencesRepository
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.engine.GameEngineConfig
import com.batodev.arrows.engine.GameEngineFeatures
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope

data class GameWonStateParams(
    val engine: GameEngine,
    val repository: UserPreferencesRepository,
    val activity: Activity?,
    val application: ArrowsApplication,
    val isAdFree: Boolean
)

data class CustomGameParams(
    val isCustom: Boolean,
    val customWidth: Int?,
    val customHeight: Int?,
    val customShape: String?
)

data class GameAreaParams(
    val engine: GameEngine,
    val tapAnimations: SnapshotStateList<TapAnimationState>,
    val guidanceAlpha: Float,
    val showGuidanceLines: Boolean,
    val themeColors: ThemeColors,
    val rewardAdManager: RewardAdManager,
    val activity: Activity?,
    val isAdFree: Boolean,
    val onToggleGuidance: () -> Unit
)

data class GameScreenContentParams(
    val engine: GameEngine,
    val activity: Activity?,
    val context: android.content.Context,
    val tapAnimations: SnapshotStateList<TapAnimationState>,
    val guidanceAlpha: Float,
    val showGuidanceLines: Boolean,
    val themeColors: ThemeColors,
    val rewardAdManager: RewardAdManager,
    val isAdFree: Boolean,
    val handleHint: () -> Unit,
    val onToggleGuidance: () -> Unit
)

fun extractCustomGameParams(intent: android.content.Intent?): CustomGameParams {
    val isCustom = intent?.getBooleanExtra("IS_CUSTOM", false) ?: false
    val customWidth = intent?.getIntExtra("CUSTOM_WIDTH", 0)?.takeIf { it > 0 }
    val customHeight = intent?.getIntExtra("CUSTOM_HEIGHT", 0)?.takeIf { it > 0 }
    val customShape = intent?.getStringExtra("CUSTOM_SHAPE")
    return CustomGameParams(isCustom, customWidth, customHeight, customShape)
}

fun createGameEngine(
    coroutineScope: CoroutineScope,
    view: android.view.View,
    context: Context,
    repository: UserPreferencesRepository,
    customParams: CustomGameParams
): GameEngine {
    return GameEngine(
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

@Composable
fun BoxScope.GuidanceToggleButton(
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


