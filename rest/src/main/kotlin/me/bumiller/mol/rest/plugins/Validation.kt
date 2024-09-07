package me.bumiller.mol.rest.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

/**
 * Sets up the request validation plugin
 */
internal fun Application.requestValidation() {
    install(RequestValidation) {

    }
}