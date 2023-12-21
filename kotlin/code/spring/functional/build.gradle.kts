plugins {
  application
  base
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
  alias(libs.plugins.spotless)
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
    ktfmt("0.46").googleStyle()
  }
}

kotlin { jvmToolchain(21) }

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks {
  wrapper { gradleVersion = "8.5" }
  compileKotlin { kotlinOptions { freeCompilerArgs += "-Xcontext-receivers" } }
  test { useJUnitPlatform() }
}
