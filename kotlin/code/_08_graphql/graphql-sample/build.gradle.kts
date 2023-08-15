import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  base
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.spotless)
  alias(libs.plugins.ktor)
}

application {
  mainClass.set("$group.ApplicationKt")
  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
  implementation(libs.bundles.ktor)
  implementation(libs.bundles.arrow)
  implementation(libs.bundles.kgraphql)
  implementation(libs.bundles.sqldelight)
  implementation(libs.suspendapp)
  implementation(libs.logback)
}

sqldelight {
  databases {
    create("SqlDelight") {
      packageName.set("$group.sqldelight")
      dialect(libs.sqldelight.postgresql.asString)
    }
  }
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.freeCompilerArgs += listOf("-Xcontext-receivers")
}

spotless {
  kotlin {
    target("**/*.kt")
    target("**/*.kts")
    ktfmt().googleStyle()
  }
}
