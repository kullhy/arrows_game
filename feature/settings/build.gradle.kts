plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "com.batodev.arrows.feature.settings"
    compileSdk = 36
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildTypes {
        release {
            buildConfigField("Boolean", "DRAW_DEBUG_STUFF", "false")
        }
        debug {
            buildConfigField("Boolean", "DRAW_DEBUG_STUFF", "false")
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

aboutLibraries {
    // Plugin will auto-generate aboutlibraries.json
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.appyx.core)
    implementation(libs.play.review.ktx)
    implementation(project(":feature:home"))
    implementation(project(":core:ui"))
    implementation(project(":ads"))
    implementation(project(":core:resources"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.reflect)
}
