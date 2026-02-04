package com.batodev.arrows.ads

import android.app.Activity
import android.content.Context
import com.batodev.arrows.GameConstants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RewardAdManager(private val context: Context) {
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    fun loadRewardAd() {
        if (_isAdLoading.value || _isAdLoaded.value) return

        _isAdLoading.value = true
        val adRequest = AdRequest.Builder().build()

        // Load rewarded interstitial ad
        RewardedInterstitialAd.load(
            context,
            GameConstants.REWARDED_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    _isAdLoaded.value = true
                    _isAdLoading.value = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedInterstitialAd = null
                    _isAdLoaded.value = false
                    _isAdLoading.value = false
                }
            }
        )
    }

    fun showRewardAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        rewardedInterstitialAd?.let { ad ->
            ad.show(activity) { _ ->
                // User earned the reward
                onRewarded()
                _isAdLoaded.value = false
                rewardedInterstitialAd = null
                // Load next ad
                loadRewardAd()
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdDismissed()
                    _isAdLoaded.value = false
                    rewardedInterstitialAd = null
                    // Load next ad
                    loadRewardAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    onAdDismissed()
                    _isAdLoaded.value = false
                    rewardedInterstitialAd = null
                }
            }
        } ?: run {
            // Ad not loaded, try loading again
            loadRewardAd()
            onAdDismissed()
        }
    }
}
