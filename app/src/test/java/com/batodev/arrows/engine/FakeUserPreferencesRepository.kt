package com.batodev.arrows.engine

import com.batodev.arrows.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository : UserPreferencesRepository(
    androidx.datastore.core.DataStoreFactory.create(
        serializer = object : androidx.datastore.core.Serializer<androidx.datastore.preferences.core.Preferences> {
            override val defaultValue: androidx.datastore.preferences.core.Preferences
                get() = androidx.datastore.preferences.core.emptyPreferences()
            override suspend fun readFrom(input: java.io.InputStream): androidx.datastore.preferences.core.Preferences = defaultValue
            override suspend fun writeTo(t: androidx.datastore.preferences.core.Preferences, output: java.io.OutputStream) {}
        }
    ) { null!! }
) {
    val themeFlow = MutableStateFlow("Dark")
    override val theme: Flow<String> = themeFlow

    override val isVibrationEnabled: Flow<Boolean> = MutableStateFlow(true)
    override val initialLevel: Flow<String?> = MutableStateFlow(null)
    override val currentLevel: Flow<String?> = MutableStateFlow(null)

    override suspend fun saveThemePreference(theme: String) {
        themeFlow.value = theme
    }

    override suspend fun saveVibrationPreference(enabled: Boolean) {
        // No-op for now or update flow
    }

    override suspend fun saveInitialLevel(levelJson: String) {
        (initialLevel as MutableStateFlow).value = levelJson
    }

    override suspend fun saveCurrentLevel(levelJson: String) {
        (currentLevel as MutableStateFlow).value = levelJson
    }

    override suspend fun clearSavedLevel() {
        (initialLevel as MutableStateFlow).value = null
        (currentLevel as MutableStateFlow).value = null
    }
}
