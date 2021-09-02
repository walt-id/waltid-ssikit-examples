plugins {
    kotlin("jvm") version "1.5.21"
    java
    application
}

group = "id.walt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.walt.id/repository/waltid/")
    maven("https://maven.walt.id/repository/waltid-ssi-kit/")
    maven("https://maven.walt.id/repository/danubetech")
    maven("https://repo.danubetech.com/repository/maven-public/")
    maven("https://jitpack.io")
    mavenLocal()
}

dependencies {
    // Walt.ID
    implementation("id.walt:waltid-ssi-kit:1.0-SNAPSHOT")
    implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.0.1")
    implementation("id.walt:waltid-ssikit-vclib:1.4.5")

    // Kotlin
    implementation(kotlin("stdlib"))

    // JSON
    implementation("com.beust:klaxon:5.5")

    // Testing
    testImplementation("io.kotest:kotest-runner-junit5:4.6.1")
    testImplementation("io.kotest:kotest-assertions-core:4.6.1")
    testImplementation("io.kotest:kotest-assertions-json:4.6.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
