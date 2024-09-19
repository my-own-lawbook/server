plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

group = "me.bumiller.mol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.serialization.json)
}

kotlin {
    jvmToolchain(22)
}