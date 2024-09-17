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
    implementation(libs.kotlin.serialization.json)

    testImplementation(libs.bundles.test)
}

tasks.test {
    useJUnitPlatform()
}