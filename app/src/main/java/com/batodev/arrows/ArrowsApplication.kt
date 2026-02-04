package com.batodev.arrows

import android.app.Application
import com.batodev.arrows.ads.InterstitialAdManager
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.data.UserPreferencesRepository
import com.batodev.arrows.data.dataStore
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArrowsApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var rewardAdManager: RewardAdManager
        private set
    lateinit var interstitialAdManager: InterstitialAdManager
        private set

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        rewardAdManager = RewardAdManager(this)
        interstitialAdManager = InterstitialAdManager(this)

        // Initialize AdMob SDK on main thread (required for ad loading)
        CoroutineScope(Dispatchers.Main).launch {
            MobileAds.initialize(this@ArrowsApplication)
            // Preload first ads
            rewardAdManager.loadRewardAd()
            interstitialAdManager.loadInterstitialAd()
        }
    }
}
