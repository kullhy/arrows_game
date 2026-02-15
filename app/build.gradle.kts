import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.detekt)
    id("jacoco")
}

android {
    namespace = "com.batodev.arrows"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.batodev.arrows"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "DRAW_DEBUG_STUFF", "false")
            // Production Ad IDs - replace with real IDs before release
            manifestPlaceholders["admobAppId"] = "ca-app-pub-9667420067790140~5728073317"
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-9667420067790140/3105779401\"")
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-9667420067790140/6849583291\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-9667420067790140/3915454308\"")
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            buildConfigField("Boolean", "DRAW_DEBUG_STUFF", "false")
            // Test Ad IDs for development
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

aboutLibraries {
    // Plugin will auto-generate aboutlibraries.json
}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

jacoco {
    toolVersion = libs.versions.jacocoVersion.get()
}

tasks.register<JacocoReport>("testDebugUnitTestCoverage") {
    dependsOn("testDebugUnitTest")
    group = "Reporting"
    description = "Generate Jacoco coverage reports for the debug build."

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val excludes = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/data/models/*"
    )

    val javaClasses = fileTree("${project.layout.buildDirectory.get().asFile}/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude(excludes)
    }
    val kotlinClasses = fileTree("${project.layout.buildDirectory.get().asFile}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(excludes)
    }

    classDirectories.setFrom(files(javaClasses, kotlinClasses))

    sourceDirectories.setFrom(files(
        "$projectDir/src/main/java",
        "$projectDir/src/main/kotlin"
    ))

    executionData.setFrom(fileTree("${project.layout.buildDirectory.get().asFile}/outputs/unit_test_code_coverage/debugUnitTest") {
        include("*.exec")
    })
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.konfetti.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.play.review.ktx)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.gson)
    implementation(libs.aboutlibraries.compose.m3)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.reflect)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
