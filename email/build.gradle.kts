plugins {
    kotlin("jvm")
    alias(libs.plugins.jacoco)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.apache.email)
    implementation(libs.koin)

    implementation(project(":model"))
}

kotlin {
    jvmToolchain(21)
}