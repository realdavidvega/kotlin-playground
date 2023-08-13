import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.ktor)
}

group = "com.realdavidvega"
version = "0.0.1"

application {
    mainClass.set("com.realdavidvega.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.sqldelight)
    implementation(libs.suspendapp)
    implementation(libs.logback)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf("-Xcontext-receivers")
}

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle()
    }
}
