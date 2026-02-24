plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.batodev.arrows.core.resources"
    compileSdk = 36
    defaultConfig {
        minSdk = 29
    }
}
