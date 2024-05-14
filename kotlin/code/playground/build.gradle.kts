@file:Suppress("UnstableApiUsage")

import io.ktor.plugin.features.*

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val h2_version: String by project
val kotest_version: String by project
val kotest_ktor: String by project
val mockk_version: String by project
val date_version: String by project
val dotenv_version: String by project
val arrow_version: String by project
val logging_version: String by project
val reactor_version: String by project
val coroutines_version: String by project

plugins {
  kotlin("jvm") version "1.9.21"
  id("io.ktor.plugin") version "2.3.6"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
  id("com.diffplug.spotless") version "6.22.0"
}

group = "playground"

version = "0.0.1"

kotlin { jvmToolchain(21) }

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

application {
  mainClass.set("playground.MainKt")
  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
  docker {
    jreVersion.set(javaVersion)
    localImageName.set("playground-app")
    imageTag.set("0.0.1-preview")
  }
}

repositories {
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
  implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-resources:$ktor_version")
  implementation("io.ktor:ktor-server-default-headers:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
  implementation("io.ktor:ktor-client-core:$ktor_version")
  implementation("io.ktor:ktor-client-cio:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:$date_version")
  implementation("io.arrow-kt:arrow-core:$arrow_version")
  implementation("io.arrow-kt:arrow-fx-coroutines:$arrow_version")
  implementation("io.arrow-kt:arrow-fx-stm:$arrow_version")
  implementation("io.arrow-kt:arrow-atomic:$arrow_version")
  implementation("io.arrow-kt:arrow-resilience:$arrow_version")
  implementation("io.projectreactor:reactor-core:$reactor_version")
  implementation("io.github.oshai:kotlin-logging-jvm:$logging_version")
  testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
  testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
  testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
  testImplementation("io.kotest.extensions:kotest-assertions-ktor:$kotest_ktor")
}

spotless {
  kotlin {
    target("**/*.kt")
    target("**/*.kts")
    ktfmt("0.46").googleStyle()
  }
}

tasks {
  wrapper { gradleVersion = "8.5" }
  compileKotlin { kotlinOptions { freeCompilerArgs += "-Xcontext-receivers" } }
  test { useJUnitPlatform() }
}
