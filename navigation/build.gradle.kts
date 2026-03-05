plugins {
    id("arrows.android.library.compose")
    id("kotlin-parcelize")
}

android {
    namespace = "com.batodev.arrows.navigation"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.appyx.core)
    implementation(project(":feature:home"))
    implementation(project(":feature:game"))
    implementation(project(":feature:generate"))
    implementation(project(":feature:settings"))
    implementation(project(":ads"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.appyx.testing.unit.common)
    testImplementation(libs.appyx.testing.junit5)
}
