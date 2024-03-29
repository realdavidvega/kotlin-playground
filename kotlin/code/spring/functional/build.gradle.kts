plugins {
  application
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
  alias(libs.plugins.spotless)
  alias(libs.plugins.gradle.ktlint)
}

repositories { mavenCentral() }

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
  kotlin { ktfmt(libs.versions.ktfmt.get()).googleStyle() }
  kotlinGradle { ktfmt(libs.versions.ktfmt.get()).googleStyle() }
}

ktlint { version.set(libs.versions.ktlint.get()) }

kotlin { jvmToolchain(21) }

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks {
  wrapper {
    gradleVersion = libs.versions.gradle.wrapper.get()
    distributionType = Wrapper.DistributionType.BIN
  }
  compileKotlin { kotlinOptions.freeCompilerArgs += listOf("-Xcontext-receivers") }
  test { useJUnitPlatform() }
}
