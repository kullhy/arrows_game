package com.batodev.arrows

import android.app.Application
import androidx.room.Room
import com.batodev.arrows.ads.ConsentManager
import com.batodev.arrows.ads.InterstitialAdManager
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.data.AppDatabase
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.MIGRATION_1_2
import com.batodev.arrows.data.UserPreferencesEntity
import com.batodev.arrows.data.UserPreferencesRepository
import com.batodev.arrows.data.migrateFromDataStoreIfNeeded
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
    lateinit var consentManager: ConsentManager
        private set

    private var isAdsInitialized = false

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "arrows_db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
        val dao = database.userPreferencesDao()
        gameStateDao = database.gameStateDao()

        // Migrate old DataStore data if present, then seed defaults
        CoroutineScope(Dispatchers.IO).launch {
            migrateFromDataStoreIfNeeded(this@ArrowsApplication, dao)
            dao.insertDefault(UserPreferencesEntity())
        }

        userPreferencesRepository = UserPreferencesRepository(dao)
        rewardAdManager = RewardAdManager(this)
        interstitialAdManager = InterstitialAdManager(this)
        consentManager = ConsentManager(this)
    }

    fun initializeAds() {
        if (isAdsInitialized) return
        isAdsInitialized = true

        CoroutineScope(Dispatchers.Main).launch {
            MobileAds.initialize(this@ArrowsApplication)
            rewardAdManager.loadRewardAd()
            interstitialAdManager.loadInterstitialAd()
        }
    }
}
