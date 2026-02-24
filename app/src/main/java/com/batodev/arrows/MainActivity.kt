package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.batodev.arrows.navigation.RootNode
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.ActivityIntegrationPoint
import com.bumble.appyx.core.integrationpoint.IntegrationPointProvider

class MainActivity : ComponentActivity(), IntegrationPointProvider {

    override lateinit var appyxV1IntegrationPoint: ActivityIntegrationPoint
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appyxV1IntegrationPoint = ActivityIntegrationPoint(this, savedInstanceState)
        enableEdgeToEdge()
        val application = applicationContext as ArrowsApplication
        val appViewModel = ViewModelProvider(
            this,
            AppViewModel.Factory(application.userPreferencesRepository, application.gameStateDao)
        )[AppViewModel::class.java]

        application.consentManager.gatherConsent(this) {
            if (application.consentManager.canRequestAds) {
                application.initializeAds()
            }
        }

        if (application.consentManager.canRequestAds) {
            application.initializeAds()
        }

        setContent {
            val currentTheme by appViewModel.theme.collectAsState()
            ArrowsTheme(themeName = currentTheme) {
                NodeHost(integrationPoint = appyxV1IntegrationPoint) { buildContext ->
                    RootNode(
                        buildContext = buildContext,
                        appViewModel = appViewModel,
                        rewardAdManager = application.rewardAdManager,
                        interstitialAdManager = application.interstitialAdManager,
                        consentManager = application.consentManager,
                        userPreferencesRepository = application.userPreferencesRepository,
                        gameStateDao = application.gameStateDao
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        appyxV1IntegrationPoint.onSaveInstanceState(outState)
    }
}
