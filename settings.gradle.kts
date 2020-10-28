rootProject.name = "spring-kotlin-exposed"
include("rest-api")

pluginManagement {
    // see: https://github.com/ilya40umov/KotLink/blob/master/settings.gradle.kts

    val kotlinVersion = "1.4.10"
    val springBootVersion = "2.3.4.RELEASE"

    plugins {
        kotlin("jvm") version kotlinVersion
        id("tanvd.kosogor") version "1.0.7"
        id("io.gitlab.arturbosch.detekt") version "1.14.2"
        id("org.owasp.dependencycheck") version "5.3.2.1"
        id("com.avast.gradle.docker-compose") version "0.13.4"
        id("com.github.ben-manes.versions") version "0.33.0"
        id("org.jetbrains.dokka") version "0.10.1"
        // spring
        id("io.spring.dependency-management") version "1.0.10.RELEASE"
        id("org.springframework.boot") version springBootVersion

        // spring-kotlin
        // kotlin: spring (proxy) related plugins see: https://kotlinlang.org/docs/reference/compiler-plugins.html
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion

        // ak-artifactory
        //id("com.jfrog.artifactory") version "4.9.6"
    }


    resolutionStrategy {
        eachPlugin {
            if(requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            }
            if (requested.id.id == "org.springframework.boot") {
                useVersion(springBootVersion)
            }
        }
    }
}
