package com.batodev.arrows.ui

import android.app.Activity
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.data.UserPreferencesRepository
import com.batodev.arrows.ui.theme.ThemeColors
import kotlinx.coroutines.CoroutineScope

data class AdSettingsSectionState(
    val repository: UserPreferencesRepository,
    val rewardAdManager: RewardAdManager,
    val themeColors: ThemeColors,
    val activity: Activity?,
    val coroutineScope: CoroutineScope,
    val rewardAdCount: Int,
    val isAdLoaded: Boolean,
    val isAdLoading: Boolean
)
