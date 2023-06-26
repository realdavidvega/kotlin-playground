plugins {
    kotlin("jvm") version "1.8.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.projectreactor:reactor-core:3.5.3")
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-22")
    implementation("org.slf4j:slf4j-simple:2.0.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}