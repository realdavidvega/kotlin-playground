import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    base
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotless)
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.spring)
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.reactor)
    implementation(libs.r2dbc.postgresql)
    testImplementation(libs.spring.test)
    testImplementation(libs.reactor.test)
}

spotless {
    kotlin {
        ktfmt("0.46").googleStyle()
    }
}

kotlin { jvmToolchain(17) }

tasks.test { useJUnitPlatform() }

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf("-Xcontext-receivers")
}
