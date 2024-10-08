plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jacoco)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.serialization.json)
}

kotlin {
    jvmToolchain(21)
}