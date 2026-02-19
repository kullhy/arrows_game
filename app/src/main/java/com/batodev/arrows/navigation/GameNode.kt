package com.batodev.arrows.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.batodev.arrows.ArrowsApplication
import com.batodev.arrows.ArrowsGameView
import com.batodev.arrows.CustomGameParams
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import androidx.compose.runtime.CompositionLocalProvider

class GameNode(
    buildContext: BuildContext,
    private val appViewModel: AppViewModel,
    private val application: ArrowsApplication,
    private val customParams: CustomGameParams,
    private val onBack: () -> Unit
) : Node(buildContext) {

    private val nodeViewModelStore = ViewModelStore()

    @Composable
    override fun View(modifier: Modifier) {
        DisposableEffect(Unit) {
            onDispose { nodeViewModelStore.clear() }
        }
        val nodeViewModelStoreOwner = remember {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = nodeViewModelStore
            }
        }
        CompositionLocalProvider(LocalViewModelStoreOwner provides nodeViewModelStoreOwner) {
            GameContent(modifier)
        }
    }

    @Composable
    private fun GameContent(modifier: Modifier) {
        val isAdFree by appViewModel.isAdFree.collectAsState()
        val themeColors = LocalThemeColors.current
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = themeColors.background
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                ArrowsGameView(
                    appViewModel = appViewModel,
                    isAdFree = isAdFree,
                    rewardAdManager = application.rewardAdManager,
                    customParams = customParams,
                    onBack = onBack
                )
            }
        }
    }
}
