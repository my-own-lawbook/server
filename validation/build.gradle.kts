plugins {
    kotlin("jvm")
    alias(libs.plugins.jacoco)
}

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
    jvmToolchain(21)
}