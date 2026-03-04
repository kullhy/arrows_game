plugins {
    id("arrows.kotlin.jvm")
}

dependencies {
    api(project(":core:models"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}
