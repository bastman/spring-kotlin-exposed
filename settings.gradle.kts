include("rest-api")

// "copy pasta" from: https://github.com/ilya40umov/KotLink/blob/master/settings.gradle.kts

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                gradle.rootProject.extra["kotlinVersion"]?.let { useVersion(it as String) }
            } else if (requested.id.id == "org.springframework.boot") {
                gradle.rootProject.extra["springBootVersion"]?.let { useVersion(it as String) }
            }
        }
    }
}