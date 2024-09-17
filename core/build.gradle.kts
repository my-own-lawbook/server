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

    implementation(libs.kotlin.datetime)
    implementation(libs.java.bcrypt)
    implementation(libs.java.jwt)
    implementation(libs.h2)

    implementation(project(":database"))
    implementation(project(":model"))
    implementation(project(":email"))
    implementation(project(":common"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}