package com.batodev.arrows

import android.app.Application
import com.batodev.arrows.data.UserPreferencesRepository

class ArrowsApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(this)
    }
}
