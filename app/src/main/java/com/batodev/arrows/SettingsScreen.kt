package com.batodev.arrows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.ui.AnimationSpeedSelectionDialog
import com.batodev.arrows.ui.AppNavigationBar
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.DebugMenu
import com.batodev.arrows.ui.FeedbackSection
import android.app.Activity
import com.batodev.arrows.ui.LegalSection
import com.batodev.arrows.ui.NavigationDestination
import com.batodev.arrows.ui.PreferencesParams
import com.batodev.arrows.ui.PreferencesSection
import com.batodev.arrows.ui.PurchasesSection
import com.batodev.arrows.ui.ThemeSelectionDialog
import com.batodev.arrows.ui.ThirdPartyLicensesDialog
import com.batodev.arrows.ui.ads.BannerAdView
import com.batodev.arrows.ui.theme.LocalThemeColors

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    rewardAdManager: RewardAdManager,
    onNavigateHome: () -> Unit = {},
    onNavigateToGenerate: () -> Unit = {}
) {
    val context = LocalContext.current
    val levelNumber by viewModel.levelNumber.collectAsState()
    val isAdFree by viewModel.isAdFree.collectAsState()
    val themeColors = LocalThemeColors.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    val currentTheme by viewModel.theme.collectAsState()
    val currentSpeed by viewModel.animationSpeed.collectAsState()

    SettingsDialogs(
        SettingsDialogsState(
            showThemeDialog, { showThemeDialog = it }, currentTheme, { viewModel.saveTheme(it) },
            showSpeedDialog, { showSpeedDialog = it }, currentSpeed, { viewModel.saveAnimationSpeed(it) }
        )
    )
    if (showLicensesDialog) {
        ThirdPartyLicensesDialog(onDismiss = { showLicensesDialog = false })
    }

    SettingsScaffold(
        SettingsScaffoldParams(
            viewModel, rewardAdManager, context, themeColors,
            levelNumber, isAdFree, currentTheme, currentSpeed,
            { showThemeDialog = true }, { showSpeedDialog = true }, { showLicensesDialog = true },
            onNavigateHome, onNavigateToGenerate
        )
    )
}

private data class SettingsScaffoldParams(
    val viewModel: AppViewModel,
    val rewardAdManager: RewardAdManager,
    val context: android.content.Context,
    val themeColors: com.batodev.arrows.ui.theme.ThemeColors,
    val levelNumber: Int,
    val isAdFree: Boolean,
    val currentTheme: String,
    val currentSpeed: String,
    val onThemeClick: () -> Unit,
    val onSpeedClick: () -> Unit,
    val onLicensesClick: () -> Unit,
    val onNavigateHome: () -> Unit,
    val onNavigateToGenerate: () -> Unit
)

@Composable
private fun SettingsScaffold(params: SettingsScaffoldParams) {
    Scaffold(
        containerColor = params.themeColors.background,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!params.isAdFree) {
                    BannerAdView()
                }
                AppNavigationBar(
                    selectedDestination = NavigationDestination.SETTINGS,
                    levelNumber = params.levelNumber,
                    themeColors = params.themeColors,
                    onNavigateHome = params.onNavigateHome,
                    onNavigateToGenerate = params.onNavigateToGenerate,
                    onNavigateToSettings = {}
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            PreferencesSection(
                PreferencesParams(
                    params.viewModel, params.themeColors, params.currentTheme, params.currentSpeed,
                    params.onThemeClick, params.onSpeedClick
                )
            )
            FeedbackSection(params.context, params.themeColors)
            PurchasesSection(
                viewModel = params.viewModel,
                rewardAdManager = params.rewardAdManager,
                themeColors = params.themeColors
            )
            val consentManager = (params.context.applicationContext as ArrowsApplication).consentManager
            LegalSection(
                params.context,
                params.themeColors,
                params.onLicensesClick,
                showPrivacyOptions = consentManager.isPrivacyOptionsRequired,
                onPrivacyOptionsClick = {
                    (params.context as? Activity)?.let { activity ->
                        consentManager.showPrivacyOptionsForm(activity) { }
                    }
                }
            )
            if (BuildConfig.DRAW_DEBUG_STUFF) DebugMenu(params.viewModel)
        }
    }
}

private data class SettingsDialogsState(
    val showThemeDialog: Boolean,
    val onShowThemeDialog: (Boolean) -> Unit,
    val currentTheme: String,
    val onThemeSelected: (String) -> Unit,
    val showSpeedDialog: Boolean,
    val onShowSpeedDialog: (Boolean) -> Unit,
    val currentSpeed: String,
    val onSpeedSelected: (String) -> Unit
)

@Composable
private fun SettingsDialogs(state: SettingsDialogsState) {
    if (state.showThemeDialog) {
        ThemeSelectionDialog(state.currentTheme, { state.onShowThemeDialog(false) }) {
            state.onThemeSelected(it)
            state.onShowThemeDialog(false)
        }
    }
    if (state.showSpeedDialog) {
        AnimationSpeedSelectionDialog(state.currentSpeed, { state.onShowSpeedDialog(false) }) {
            state.onSpeedSelected(it)
            state.onShowSpeedDialog(false)
        }
    }
}
