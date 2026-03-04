plugins {
    id("arrows.android.feature")
}

android {
    namespace = "com.batodev.arrows.feature.generate"
}

dependencies {
    implementation(project(":feature:home"))
    implementation(project(":data"))
    implementation(project(":core:ui"))
    implementation(project(":core:resources"))
    testImplementation(libs.junit)
}
