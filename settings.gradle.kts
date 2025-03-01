plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "civoris-server"
include("database")
include("model")
include("common")
include("email")
include("core")
include("rest")
include("validation")
