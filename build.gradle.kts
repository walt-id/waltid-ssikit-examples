plugins {
    kotlin("jvm") version "1.6.21"
    java
    application
}

group = "id.walt"
// Same as SSI Kit version
version = "1.7.0"

repositories {
    mavenCentral()
    maven("https://maven.walt.id/repository/waltid/")
    maven("https://maven.walt.id/repository/waltid-ssi-kit/")
    maven("https://repo.danubetech.com/repository/maven-public/")
    maven("https://jitpack.io")
    mavenLocal()
}

dependencies {
    // walt.id
    implementation("id.walt:waltid-ssi-kit:1.13.0-SNAPSHOT")
    implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.1.2")
    implementation("id.walt:waltid-ssikit-vclib:1.23.5")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.0-alpha5")

    // Kotlin
    implementation(kotlin("stdlib"))

    // JSON
    implementation("com.beust:klaxon:5.6")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
