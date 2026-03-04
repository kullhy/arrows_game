plugins {
    id("arrows.android.library")
}

android {
    namespace = "com.batodev.arrows.core.testing"
}

dependencies {
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(project(":data"))
    api(project(":core:models"))
}
