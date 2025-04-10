@file:Suppress("UnstableApiUsage")

plugins {
  base
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ktor)
  alias(libs.plugins.spotless)
  alias(libs.plugins.gradle.ktlint)
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation(libs.bundles.kotlin)
  implementation(libs.bundles.arrow)
  implementation(libs.bundles.reactor)
  implementation(libs.bundles.ktor)
  implementation(libs.bundles.xef)
  implementation(libs.bundles.langchain4j)
  implementation(libs.cache4k)
  implementation(libs.logback)
  testImplementation(libs.bundles.kotest)
  testImplementation(libs.bundles.testcontainers)
}

kotlin { jvmToolchain(21) }

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

spotless {
  kotlin { ktfmt(libs.versions.ktfmt.get()).googleStyle() }
  kotlinGradle { ktfmt(libs.versions.ktfmt.get()).googleStyle() }
}

ktlint { version.set(libs.versions.ktlint.get()) }

tasks {
  val compilerFlags = listOf("-Xcontext-receivers", "-Xconsistent-data-class-copy-visibility")
  compileKotlin { compilerOptions { freeCompilerArgs.addAll(compilerFlags) } }
  compileTestKotlin { compilerOptions { freeCompilerArgs.addAll(compilerFlags) } }
  test { useJUnitPlatform() }
}
