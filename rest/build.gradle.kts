plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jacoco)
}

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

    testImplementation(libs.bundles.ktor.test)
    testImplementation(libs.bundles.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}