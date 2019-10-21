plugins {
    id("tanvd.kosogor") apply false
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
       // maven { setUrl("https://dl.bintray.com/kotlin/exposed") }
    }
    apply(plugin = "tanvd.kosogor")
}
