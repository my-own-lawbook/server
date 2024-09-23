package me.bumiller.mol.rest.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Sets up the cors-configuration
 */
internal fun Application.cors() {
    install(CORS) {
        anyHost()
    }
}