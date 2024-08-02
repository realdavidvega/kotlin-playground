pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// root project
val projectName = "langchain"
rootProject.name = projectName

// sub-projects
include("$projectName-core")
project(":$projectName-core").projectDir = file("core")
