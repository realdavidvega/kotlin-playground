
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// root project
val projectName = "playground"
rootProject.name = projectName

// sub-projects
include("$projectName-lang")
project(":$projectName-lang").projectDir = file("modules/lang")

include("$projectName-ai")
project(":$projectName-ai").projectDir = file("modules/ai")

include("$projectName-trading")
project(":$projectName-trading").projectDir = file("modules/trading")
