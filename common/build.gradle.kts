plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jacoco)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.serialization.json)

    testImplementation(libs.bundles.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}