plugins {
    kotlin("jvm") version "1.6.10"
    java
    application
}

group = "id.walt"
version = "1.3.0"

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
    implementation("id.walt:waltid-ssi-kit:1.6.2")
    implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.1.0")
    implementation("id.walt:waltid-ssikit-vclib:1.14.1")

    // Kotlin
    implementation(kotlin("stdlib"))

    // JSON
    implementation("com.beust:klaxon:5.5")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
