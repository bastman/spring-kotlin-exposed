import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Duration

plugins {
    // built-in plugins
    java
    jacoco
    id("project-report")
    application
    // versions of all kotlin plugins are resolved by logic in 'settings.gradle.kts'
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt") apply true
    id("org.owasp.dependencycheck") apply true
    id("com.avast.gradle.docker-compose") apply true
    id("com.github.ben-manes.versions") apply true
    id("org.jetbrains.dokka") apply false

    // spring
    // kotlin: spring (proxy) related plugins see: https://kotlinlang.org/docs/reference/compiler-plugins.html
    id("org.jetbrains.kotlin.plugin.spring") apply true
    id("org.jetbrains.kotlin.plugin.noarg") apply true
    id("org.jetbrains.kotlin.plugin.allopen") apply true
    // spring
    id("io.spring.dependency-management") apply true
    id("org.springframework.boot") apply true
    // id("com.jfrog.artifactory") apply true
}

group = "com.example.restapi"
version = "0.0.1"

application {
    mainClassName = "com.example.MainKt"
}
repositories {
    // artifactory
    /*
    maven {
        url = uri("https://<YOUR_COMPANY>.jfrog.io/<YOUR_COMPANY>/libs-release")
        credentials {
            username = System.getenv("ARTIFACTORY_USERNAME")
                    .also { if (it.isNullOrBlank()) { error("ENV-VAR NOT SET: ARTIFACTORY_USERNAME") } }
            password = System.getenv("ARTIFACTORY_PASSWORD")
                    .also { if (it.isNullOrBlank()) { error("ENV-VAR NOT SET: ARTIFACTORY_PASSWORD") } }
        }
    }
    */
    mavenLocal()
    mavenCentral()
    jcenter()
    // maven { url=uri("https://dl.bintray.com/konform-kt/konform") }
}
dependencies {
    // kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")

    // logging
    implementation("io.github.microutils:kotlin-logging:2.0.+")
    implementation("net.logstash.logback:logstash-logback-encoder:6.+")
    val logbackJsonVersion = "0.1.5"
    implementation("ch.qos.logback.contrib:logback-json-classic:$logbackJsonVersion")
    implementation("ch.qos.logback.contrib:logback-jackson:$logbackJsonVersion")
    // monitoring
    //implementation("io.micrometer:micrometer-registry-prometheus:1.3.+")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // db: postgres driver & hikari pool & flyway
    implementation("org.postgresql:postgresql:42.2.12")
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.flywaydb:flyway-core:6.5.7")
    // db: exposed sql client
    val exposedVersion = "0.28.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jodatime:$exposedVersion")
    //implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.jetbrains.exposed:spring-transaction:$exposedVersion")

    // db: postgis:
    // https://github.com/sdeleuze/geospatial-messenger/blob/master/build.gradle.kts
    // https://postgis.net/docs/reference.
    // http://www.tsusiatsoftware.net/jts/main.html
    implementation("net.postgis:postgis-jdbc:2.5.0") {
        exclude(module = "postgresql")
    }
    /*
    implementation("com.github.mayconbordin:postgis-geojson:1.1") {
        exclude(module = "postgresql")
    }

     */
    // serialization: jackson json
    val jacksonVersion = "2.11.3"
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-modules-java8:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    // jmespath ... you know "jq" ;)
    implementation("io.burt:jmespath-jackson:0.5.0")
    // yavi: "If you are not a fan of Bean Validation, YAVI will be an awesome alternative."
    implementation("am.ik.yavi:yavi:0.4.0")

    // spring
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-jdbc") {
        exclude(group = "com.zaxxer", module = "HikariCP")
    }
    // swagger
    val swaggerVersion = "3.0.0"
    //implementation("io.springfox:springfox-swagger2:$swaggerVersion")
    //implementation("io.springfox:springfox-swagger-ui:$swaggerVersion")
    implementation("io.springfox:springfox-boot-starter:$swaggerVersion")
    implementation("io.springfox:springfox-swagger-ui:$swaggerVersion")
    // fp
    implementation("org.funktionale:funktionale-all:1.2")

    // test: junit5
    val junitVersion = "5.7.+"
    // see: https://stackoverflow.com/questions/54598484/gradle-5-junit-bom-and-spring-boot-incorrect-versions/54605523#54605523
    testImplementation(enforcedPlatform("org.junit:junit-bom:$junitVersion")) // JUnit 5 BOM
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    // test: kotlin
    //testImplementation("org.jetbrains.kotlin:kotlin-test")
    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.amshove.kluent:kluent:1.63")
    testImplementation("io.mockk:mockk:1.10.+")
    testImplementation("dev.minutest:minutest:1.11.+") // 1.4.+

    // test: spring
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.jupiter", module = "junit-jupiter")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-junit-jupiter")
        exclude(group = "org.mockito", module = "mockito-core")
    }

    /*
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
            apiVersion = "1.3"
            languageVersion = "1.3"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    withType<Test> {
        useJUnitPlatform {
            //systemProperty("spring.datasource.url", "jdbc:postgresql://localhost:45432/kotlink")
            //systemProperty("spring.redis.url", "redis://localhost:46379")
        }
        testLogging.apply {
            events("started", "passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
            showStandardStreams = true
            // remove standard output/error logging from --info builds
            // by assigning only 'failed' and 'skipped' events
            info.events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        }
        // listen to events in the test execution lifecycle
        // see: https://nwillc.wordpress.com/2019/01/08/gradle-kotlin-dsl-closures/
        beforeTest(closureOf<TestDescriptor> {
            logger.lifecycle("\t===== START TEST: ${this.className}.${this.name}")
        })
        afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
            if (descriptor.parent == null) {
                logger.lifecycle("Tests run: ${result.testCount}, Failures: ${result.failedTestCount}, Skipped: ${result.skippedTestCount}")
            }
            Unit
        }))
        doFirst {
            //dockerCompose.exposeAsEnvironment(project.tasks.named("test").get())
            //dockerCompose.exposeAsSystemProperties(project.tasks.named("test").get())

            // no idea, how to port that: dockerCompose.exposeAsEnvironment(test)
            // no idea, how to port that: dockerCompose.exposeAsSystemProperties(test)
            // expose db host as env variable in a bash-compliant way ...
            dockerCompose.servicesInfos.forEach {
                val k = it.key
                val v = it.value
                val envVarName = "${k.replace("-", "_").toUpperCase()}_HOST"
                val envVarValue = v.host
                println("=== dockerCompose: expose env var: $envVarName=$envVarValue")
                environment(envVarName, envVarValue)
            }
        }
    }
    withType<JacocoReport> {
        reports {
            xml.apply {
                isEnabled = true
            }
            html.apply {
                isEnabled = true
            }
        }
    }

    withType<DependencyUpdatesTask> {
        // https://github.com/ben-manes/gradle-versions-plugin
        checkForGradleUpdate = true
        outputFormatter = "plain"
        outputDir = "build/reports/dependencyUpdates"
        reportfileName = "report"
        revision = "release" // one of: release | milestone | integration
    }
}

dependencyCheck {
    // see: https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/configuration-aggregate.html
    failOnError = false
}

dockerCompose {
    isRequiredBy(project.tasks.named("test").get())
    useComposeFiles = listOf("docker/docker-compose-ci.yml") // like 'docker-compose -f <file>'
    startedServices = listOf("spring-kotlin-exposed-db-ci") // list of services to execute when calling 'docker-compose up' (when not specified, all services are executed)
    captureContainersOutput = true
    //upAdditionalArgs = ['--remove-orphans']
    //downAdditionalArgs = ['--remove-orphans']
    // captureContainersOutput = true // prints output of all containers to Gradle output - very useful for debugging
    // captureContainersOutputToFile = '/path/to/logFile' // sends output of all containers to a log file
    // stopContainers = false // doesn't call `docker-compose down` - useful for debugging
    // removeContainers = false
    // removeImages = "None" // Other accepted values are: "All" and "Local"
    removeOrphans = true // Removes containers for services not defined in the Compose file
    forceRecreate = true // pass '--force-recreate' when calling 'docker-compose up'
    removeVolumes = true
    // waitForTcpPorts = false // turns off the waiting for exposed TCP ports opening
    // projectName = 'my-project' // allow to set custom docker-compose project name (defaults to directory name)
    // executable = '/path/to/docker-compose' // allow to set the path of the docker-compose executable (useful if not present in PATH)
    // dockerExecutable = '/path/to/docker' // allow to set the path of the docker executable (useful if not present in PATH)
    // dockerComposeWorkingDirectory = '/path/where/docker-compose/is/invoked/from'
    dockerComposeStopTimeout = Duration.ofSeconds(5) // time before docker-compose sends SIGTERM to the running containers after the composeDown task has been started
    // environment.put 'BACKEND_ADDRESS', '192.168.1.100' // Pass environment variable to 'docker-compose' for substitution in compose file
    // scale = [${serviceName1}: 5, ${serviceName2}: 2] // Pass docker compose --scale option like 'docker-compose up --scale serviceName1=5 --scale serviceName2=2'


    // fix for docker-compose <1.19 ...
    // after version 0.9.5, docker-compose-plugin will add `--renew-anon-volumes` when forceRecreate is set to true. This command
    // line option was only added in docker-compose 1.19.0
    with(composeExecutor.version) {
        if (major == 1 && minor < 19) {
            upAdditionalArgs = listOf("--force-recreate")
        } else {
            forceRecreate = true // pass "--force-recreate" when calling "docker-compose up"
        }
    }

}
//dockerCompose.isRequiredBy(test) // hooks 'dependsOn composeUp' and 'finalizedBy composeDown', and exposes environment variables and system properties (if possible)


detekt {
    failFast = true // fail build on any finding
    buildUponDefaultConfig = true // preconfigure defaults
    config = files("src/main/resources/default-detekt-config.yml") // point to your custom config defining rules to run, overwriting default behavior
    //baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt

    reports {
        html.enabled = true // observe findings in your browser with structure and code snippets
        xml.enabled = true // checkstyle like format mainly for integrations like Jenkins
        txt.enabled = true // similar to the console output, contains issue signature to manually edit baseline files
    }
}
