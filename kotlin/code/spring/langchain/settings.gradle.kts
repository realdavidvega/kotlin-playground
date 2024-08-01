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

val Provider<PluginDependency>.id: String
  get() = get().pluginId

val Provider<String>.version: String
  get() = get()

val Provider<MinimalExternalModuleDependency>.asString: String
  get() = get().run {
    "${module.group}:${module.name}:${versionConstraint}"
  }
