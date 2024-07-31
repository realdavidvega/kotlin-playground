plugins {
  base
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.gradle.ktlint) apply false
}

allprojects {
  group = property("project.group").toString()
  version = property("project.version").toString()
}

tasks {
  wrapper {
    gradleVersion = libs.versions.gradle.wrapper.version
    distributionType = Wrapper.DistributionType.BIN
  }
}
