plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.kover)
}

group = "me.bumiller.mol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.apache.email)
    implementation(libs.koin)

    implementation(project(":model"))
}