plugins {
    kotlin("jvm")
}

group = "me.bumiller.mol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.koin)

    implementation(libs.kotlin.datetime)

    implementation(project(":model"))
    implementation(project(":core"))
    implementation(project(":common"))

    testImplementation(libs.junit)
    testImplementation(libs.junit.params)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(22)
}