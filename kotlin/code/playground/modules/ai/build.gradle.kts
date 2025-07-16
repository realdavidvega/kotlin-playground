@file:Suppress("UnstableApiUsage")

plugins {
  base
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.spotless)
  alias(libs.plugins.gradle.ktlint)
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation(libs.bundles.kotlin)
  implementation(libs.bundles.koog)
  implementation(libs.bundles.xef)
  implementation(libs.bundles.langchain)
  implementation(libs.logback)
  testImplementation(libs.bundles.kotest)
  testImplementation(libs.bundles.testcontainers)
}

kotlin { jvmToolchain(24) }

java {
  sourceCompatibility = JavaVersion.VERSION_24
  targetCompatibility = JavaVersion.VERSION_24
}

spotless {
  kotlin { ktfmt(libs.versions.ktfmt.get()).googleStyle() }
  kotlinGradle { ktfmt(libs.versions.ktfmt.get()).googleStyle() }
}

ktlint { version.set(libs.versions.ktlint.get()) }

tasks {
  compileKotlin { compilerOptions { freeCompilerArgs.add("-Xcontext-receivers") } }
  compileTestKotlin { compilerOptions { freeCompilerArgs.add("-Xcontext-receivers") } }
  test { useJUnitPlatform() }
}
