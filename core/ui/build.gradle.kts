plugins {
    id("arrows.android.library.compose")
}

android {
    namespace = "com.batodev.arrows.core.ui"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(project(":core:resources"))
    implementation(project(":core:models"))
}
