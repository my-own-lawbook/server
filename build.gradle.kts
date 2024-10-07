plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kover)
    alias(libs.plugins.sonarqube)
}

group = "me.bumiller.mol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.koin)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.exposed)

    implementation(libs.kotlin.serialization.json)
    implementation(libs.postgres)

    implementation(project(":database"))
    implementation(project(":email"))
    implementation(project(":model"))
    implementation(project(":rest"))
    implementation(project(":core"))
    implementation(project(":validation"))

    implementation("ch.qos.logback:logback-classic:1.5.7")

    allprojects {
        kover(this)
    }
}

kotlin {
    jvmToolchain(21)
}