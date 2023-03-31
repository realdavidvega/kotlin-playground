import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val kotlin = "1.8.10"
val wrappers = "1.0.0-pre.525"

fun wrappers(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"

plugins {
    kotlin("js") version "1.8.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform(wrappers("wrappers-bom:$wrappers")))
    implementation(wrappers("react"))
    implementation(wrappers("react-dom"))
    implementation(wrappers("emotion"))
    implementation(wrappers("react-router-dom"))
    implementation(wrappers("redux"))
    implementation(wrappers("react-redux"))
    implementation(wrappers("extensions"))
    implementation(wrappers("mui"))
    implementation(wrappers("mui-icons"))
    implementation(npm("postcss", "8.3.5"))
    implementation(npm("postcss-loader", "4.2.0"))
    implementation(npm("autoprefixer", "10.2.6"))
    implementation(npm("tailwindcss", "3.3.0"))
}

kotlin {
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
}

val copyTailwindConfig = tasks.register<Copy>("copyTailwindConfig") {
    from("./tailwind.config.js")
    into("${rootProject.buildDir}/js/packages/${rootProject.name}")

    dependsOn(":kotlinNpmInstall")
}

val copyPostcssConfig = tasks.register<Copy>("copyPostcssConfig") {
    from("./postcss.config.js")
    into("${rootProject.buildDir}/js/packages/${rootProject.name}")

    dependsOn(":kotlinNpmInstall")
}

tasks.named("processResources") {
    dependsOn(copyTailwindConfig)
    dependsOn(copyPostcssConfig)
}

tasks.withType(KotlinWebpack::class.java).forEach { t ->
    t.inputs.files(fileTree("src/jsMain/resources"))
}
