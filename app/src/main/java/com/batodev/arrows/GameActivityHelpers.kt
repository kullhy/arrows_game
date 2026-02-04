package com.batodev.arrows

import android.app.Activity
import com.batodev.arrows.data.UserPreferencesRepository
import com.batodev.arrows.engine.GameEngine

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

fun extractCustomGameParams(intent: android.content.Intent?): CustomGameParams {
    val isCustom = intent?.getBooleanExtra("IS_CUSTOM", false) ?: false
    val customWidth = intent?.getIntExtra("CUSTOM_WIDTH", 0)?.takeIf { it > 0 }
    val customHeight = intent?.getIntExtra("CUSTOM_HEIGHT", 0)?.takeIf { it > 0 }
    val customShape = intent?.getStringExtra("CUSTOM_SHAPE")
    return CustomGameParams(isCustom, customWidth, customHeight, customShape)
}
