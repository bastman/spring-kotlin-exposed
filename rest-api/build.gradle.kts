import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// copy-pasta from Ilya ;) (https://github.com/ilya40umov/KotLink/blob/master/build.gradle.kts)

plugins {
    // built-in plugins
    java
    jacoco
    id("project-report")
    application
    // versions of all kotlin plugins are resolved by logic in 'settings.gradle.kts'
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    id("org.jetbrains.dokka") version "0.9.17" apply false
    // version of spring boot plugin is also resolved by 'settings.gradle.kts'
    id("org.springframework.boot")
    // other plugins require a version to be mentioned
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC14"
    id("org.owasp.dependencycheck") version "4.0.2"
}

version = "0.0.1"

application {
    mainClassName = "com.example.MainKt"
}

repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    // val springBootVersion: String by project.extra
    // kotlin
    compile(kotlin("stdlib-jdk8"))
    // logging
    implementation("io.github.microutils:kotlin-logging:1.6.10")
    implementation("net.logstash.logback:logstash-logback-encoder:5.+")
    val logbackJsonVersion = "0.1.5"
    implementation("ch.qos.logback.contrib:logback-json-classic:$logbackJsonVersion")
    implementation("ch.qos.logback.contrib:logback-jackson:$logbackJsonVersion")
    // monitoring
    implementation("io.micrometer:micrometer-registry-prometheus:1.0.+")
    // serialization: jackson json
    val jacksonVersion =  "2.9.7"
    implementation("com.fasterxml.jackson.module:jackson-modules-java8:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    // db: postgres driver & hikari pool
    implementation("org.postgresql:postgresql:42.2.4")
    implementation("com.zaxxer:HikariCP:3.2.0")
    // db: exposed sql client
    val exposedVersion = "0.12.2"//"0.11.1"
    implementation("org.jetbrains.exposed:exposed:$exposedVersion")
    implementation("org.jetbrains.exposed:spring-transaction:$exposedVersion")
    // db: flyway db migrations
    implementation("org.flywaydb:flyway-core:5.2.0")
    // spring
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group="org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation ("org.springframework.boot:spring-boot-starter-jdbc") {
        exclude(group= "com.zaxxer", module= "HikariCP")
    }
    // swagger
    val swaggerVersion = "2.9.2"
    implementation("io.springfox:springfox-swagger2:$swaggerVersion")
    implementation("io.springfox:springfox-swagger-ui:$swaggerVersion")

    // test: junit5
    val junitVersion = "5.3.1"
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    // test: kotlin
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.amshove.kluent:kluent:1.47")
    testImplementation("io.mockk:mockk:1.9")

    // test: spring
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module= "junit")
        exclude(group="com.vaadin.external.google", module="android-json")
    }

    /*
    testCompile("org.mockito:mockito-core:2.23.4") {
        isForce = true
        because("version that is enforced by Spring Boot is not compatible with Java 11")
    }
    testCompile("net.bytebuddy:byte-buddy:1.9.3") {
        isForce = true
        because("version that is enforced by Spring Boot is not compatible with Java 11")
    }
     */
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    withType<Test> {
        useJUnitPlatform {
            //systemProperty("spring.datasource.url", "jdbc:postgresql://localhost:45432/kotlink")
            //systemProperty("spring.redis.url", "redis://localhost:46379")
        }
        testLogging.apply {
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
    withType<JacocoReport> {
        reports {
            xml.apply {
                isEnabled = true
            }
            html.apply {
                isEnabled = false
            }
        }
    }
    withType<Detekt> {
        description = "Runs Detekt code analysis"
        config = files("src/main/resources/default-detekt-config.yml")
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
