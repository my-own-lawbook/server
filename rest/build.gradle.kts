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
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.exposed)

    implementation(libs.kotlin.serialization.json)

    implementation(project(":core"))
    implementation(project(":model"))
    implementation(project(":common"))
    implementation(project(":validation"))
}

tasks.test {
    useJUnitPlatform()
}