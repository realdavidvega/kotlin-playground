val java: String = libs.versions.java.version
val ktfmt: String = libs.versions.ktfmt.version
val ktlint: String = libs.versions.ktlint.version

plugins {
  base
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spotless)
  alias(libs.plugins.gradle.ktlint)
}

repositories { mavenCentral() }

dependencies {
  implementation(libs.bundles.kotlin)
  implementation(libs.bundles.spring)
  implementation(libs.bundles.arrow)
  implementation(libs.r2dbc.postgresql)
  implementation(libs.reactor.extensions)
  testImplementation(libs.spring.test)
  testImplementation(libs.reactor.test)
}

java { toolchain { languageVersion(java) } }

kotlin { jvmToolchain { languageVersion(java) } }

spotless {
  kotlin { ktfmt(ktfmt).googleStyle() }
  kotlinGradle { ktfmt(ktfmt).googleStyle() }
}

ktlint { version.set(ktlint) }

tasks {
  val args = listOf("-Xjsr305=strict", "-Xcontext-receivers")
  compileKotlin { compilerOptions { freeCompilerArgs.addAll(args) } }
  compileTestKotlin { compilerOptions { freeCompilerArgs.addAll(args) } }
  test { useJUnitPlatform() }

  wrapper {
    gradleVersion = libs.versions.gradle.wrapper.version
    distributionType = Wrapper.DistributionType.BIN
  }
}
