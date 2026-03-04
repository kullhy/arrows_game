plugins {
    id("arrows.android.feature")
}

android {
    namespace = "com.batodev.arrows.feature.home"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    api(project(":core:ui"))
    api(project(":data"))
    api(project(":ads"))
    api(project(":domain"))
    api(project(":core:resources"))
    testImplementation(libs.appyx.testing.unit.common)
    testImplementation(libs.appyx.testing.junit5)
}
