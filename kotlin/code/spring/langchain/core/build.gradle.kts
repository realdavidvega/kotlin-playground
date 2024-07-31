@file:Suppress("UnstableApiUsage")

val java: String = libs.versions.java.version
val ktfmt: String = libs.versions.ktfmt.version
val ktlint: String = libs.versions.ktlint.version

plugins {
  base
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.gradle.ktlint)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.dgs.codegen)
}

repositories { mavenCentral() }

dependencies {
  implementation(libs.bundles.kotlin)
  implementation(libs.bundles.arrow)
  implementation(libs.bundles.spring)
  implementation(libs.bundles.reactor)
  implementation(libs.bundles.langchain4j)
  implementation(libs.bundles.postgres)
  implementation(libs.bundles.flyway)
  implementation(libs.dgs)
  implementation(libs.logback)
  implementation(libs.jackson)
  testImplementation(libs.bundles.kotest)
}

java {
  sourceCompatibility = JavaVersion.toVersion(java)
  targetCompatibility = JavaVersion.toVersion(java)
  toolchain { languageVersion(java) }
}

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

  generateJava {
    schemaPaths = mutableListOf("${projectDir}/src/main/resources/schemas")
    packageName = "langchain.graphql"
    generateClientv2 = true
  }
}
