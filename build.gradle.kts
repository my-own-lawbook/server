plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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

    implementation("ch.qos.logback:logback-classic:1.5.7")
}

kotlin {
    jvmToolchain(19)
}