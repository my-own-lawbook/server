plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "my-own-lawbook"
include("database")
include("model")
include("common")
include("email")
include("auth")
include("core")
include("rest")
include("validation")
