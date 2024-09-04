plugins {
    kotlin("jvm")
}

group = "me.bumiller.mol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.koin)
    implementation(libs.bundles.exposed)

    implementation(project(":model"))
}

kotlin {
    jvmToolchain(19)
}