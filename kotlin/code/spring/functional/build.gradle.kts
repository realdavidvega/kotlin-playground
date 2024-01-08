<<<<<<< Updated upstream
||||||| constructed merge base
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

=======

>>>>>>> Stashed changes
plugins {
  application
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
  alias(libs.plugins.spotless)
  alias(libs.plugins.ktlint.gradle)
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
  kotlin {
    target("**/*.kt")
    target("**/*.kts")
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }
}

ktlint{
  version.set(libs.versions.ktfmt.get())
}

kotlin { jvmToolchain(21) }

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks {
  wrapper {
    gradleVersion = libs.versions.gradle.get()
    distributionType = Wrapper.DistributionType.ALL
  }
  compileKotlin { kotlinOptions.freeCompilerArgs += listOf("-Xcontext-receivers") }
  test { useJUnitPlatform() }
}
