plugins {
  kotlin("jvm") version "2.0.0"
  id("com.diffplug.spotless") version "6.19.0"
}

group = "com.rockthejvm"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
  implementation("ch.qos.logback:logback-classic:1.5.6")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
  testImplementation("junit:junit:4.13.2")
}

spotless {
  kotlin { ktfmt().googleStyle() }
  kotlinGradle { ktfmt().googleStyle() }
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }
