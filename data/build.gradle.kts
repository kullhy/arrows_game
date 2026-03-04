plugins {
    id("arrows.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.batodev.arrows.data"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(project(":domain"))
    implementation(project(":core:resources"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
