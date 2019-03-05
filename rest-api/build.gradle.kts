import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Duration
//import com.avast.gradle.dockercompose.

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
    id("com.avast.gradle.docker-compose") version "0.8.8"
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
    testImplementation("dev.minutest:minutest:1.4.+")

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
        beforeTest(closureOf<TestDescriptor>{
            logger.lifecycle("\t===== START TEST: ${this.className}.${this.name}")
        })
        afterSuite(KotlinClosure2<TestDescriptor,TestResult,Unit>({descriptor, result ->
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
                val envVarName="${k.replace("-","_").toUpperCase()}_HOST"
                val envVarValue=v.host
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
    withType<Detekt> {
        description = "Runs Detekt code analysis"
        config = files("src/main/resources/default-detekt-config.yml")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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
}
//dockerCompose.isRequiredBy(test) // hooks 'dependsOn composeUp' and 'finalizedBy composeDown', and exposes environment variables and system properties (if possible)


