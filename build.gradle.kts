plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.ktor)
    alias(libs.plugins.jacoco.aggregation)
}

group = "me.bumiller.mol"
version = "1.0.0"

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

    // Includes every submodule into the jacoco aggregate
    subprojects(::jacocoAggregation)
}

tasks.test.get().finalizedBy("testCodeCoverageReport")

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_21)
        localImageName.set("my-own-lawbook")
        externalRegistry.set(
            io.ktor.plugin.features.DockerImageRegistry.externalRegistry(
                username = providers.environmentVariable("REGISTRY_USERNAME"),
                password = providers.environmentVariable("REGISTRY_PASSWORD"),
                project = providers.environmentVariable("REGISTRY_REPO"),
                namespace = providers.environmentVariable("REGISTRY_ORG"),
                hostname = providers.environmentVariable("REGISTRY")
            )
        )
    }
}