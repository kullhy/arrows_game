package com.batodev.arrows

import android.app.Application
import androidx.room.Room
import com.batodev.arrows.ads.InterstitialAdManager
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.data.AppDatabase
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.MIGRATION_1_2
import com.batodev.arrows.data.UserPreferencesEntity
import com.batodev.arrows.data.UserPreferencesRepository
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArrowsApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var gameStateDao: GameStateDao
        private set
    lateinit var rewardAdManager: RewardAdManager
        private set
    lateinit var interstitialAdManager: InterstitialAdManager
        private set
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "arrows_db")
            .addMigrations(MIGRATION_1_2)
            .build()
        val dao = database.userPreferencesDao()
        gameStateDao = database.gameStateDao()

        // Seed the default row if it doesn't exist
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertDefault(UserPreferencesEntity())
        }

        userPreferencesRepository = UserPreferencesRepository(dao)
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
