plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.6.10"
    java
    application
}

group = "id.walt"
// Same as SSI Kit version
version = "2.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.walt.id/repository/waltid/")
    maven("https://maven.walt.id/repository/waltid-ssi-kit/")
    maven("https://repo.danubetech.com/repository/maven-public/")
}

dependencies {
    // walt.id
    implementation("id.walt:waltid-ssikit:1.2303271054.0")
    implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.1.3")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

    // Kotlin
    implementation(kotlin("stdlib"))

    // JSON
    implementation("com.beust:klaxon:5.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
