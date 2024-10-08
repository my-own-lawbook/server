plugins {
    kotlin("jvm")
    alias(libs.plugins.jacoco)
}

group = "me.bumiller.mol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.koin)
    implementation(libs.bundles.exposed)

    implementation(project(":common"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.h2)

}

tasks.test.configure {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}