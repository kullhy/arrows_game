package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batodev.arrows.data.AndroidResourceBoardShapeProvider
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.ui.AnimationSpeedSelectionDialog
import com.batodev.arrows.ui.AppNavigationBar
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.DebugMenu
import com.batodev.arrows.ui.FeedbackSection
import com.batodev.arrows.ui.LegalSection
import com.batodev.arrows.ui.NavigationDestination
import com.batodev.arrows.ui.PreferencesParams
import com.batodev.arrows.ui.PreferencesSection
import com.batodev.arrows.ui.PurchasesSection
import com.batodev.arrows.ui.ThemeSelectionDialog
import com.batodev.arrows.ui.ads.BannerAdView
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.LocalThemeColors

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = applicationContext as ArrowsApplication
        val shapeProvider = AndroidResourceBoardShapeProvider(this)
        setContent {
            val viewModel: AppViewModel = viewModel(
                factory = AppViewModel.Factory(application.userPreferencesRepository)
            )
            viewModel.shapeProvider = shapeProvider
            val currentTheme by viewModel.theme.collectAsState()
            ArrowsTheme(themeName = currentTheme) {
                SettingsScreen(
                    viewModel = viewModel,
                    rewardAdManager = application.rewardAdManager
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: AppViewModel, rewardAdManager: RewardAdManager) {
    val context = LocalContext.current
    val application = context.applicationContext as ArrowsApplication
    val repository = application.userPreferencesRepository
    val levelNumber by repository.levelNumber.collectAsState(initial = 1)
    val isAdFree by repository.isAdFree.collectAsState(initial = false)
    val themeColors = LocalThemeColors.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    val currentTheme by viewModel.theme.collectAsState()
    val currentSpeed by viewModel.animationSpeed.collectAsState()

    SettingsDialogs(
        SettingsDialogsState(
            showThemeDialog, { showThemeDialog = it }, currentTheme, { viewModel.saveTheme(it) },
            showSpeedDialog, { showSpeedDialog = it }, currentSpeed, { viewModel.saveAnimationSpeed(it) }
        )
    )

    Scaffold(
        containerColor = themeColors.background,
        topBar = {
            if (!isAdFree) {
                BannerAdView()
            }
        },
        bottomBar = {
            AppNavigationBar(
                selectedDestination = NavigationDestination.SETTINGS,
                levelNumber = levelNumber,
                themeColors = themeColors
            )
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
                    viewModel, themeColors, currentTheme, currentSpeed,
                    { showThemeDialog = true }, { showSpeedDialog = true }
                )
            )
            FeedbackSection(context, themeColors)
            PurchasesSection(
                repository = repository,
                rewardAdManager = rewardAdManager,
                themeColors = themeColors
            )
            LegalSection(context, themeColors)
            if (BuildConfig.DRAW_DEBUG_STUFF) DebugMenu(viewModel)
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
