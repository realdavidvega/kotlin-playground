
allprojects {
  group = property("project.group").toString()
  version = property("project.version").toString()
}

tasks {
  wrapper {
    gradleVersion = libs.versions.gradle.wrapper.get()
    distributionType = Wrapper.DistributionType.BIN
  }
}
