plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kover)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.ktor)
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
    implementation(libs.logback)

    implementation(project(":database"))
    implementation(project(":email"))
    implementation(project(":model"))
    implementation(project(":rest"))
    implementation(project(":core"))
    implementation(project(":validation"))


    allprojects {
        kover(this)
    }
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}