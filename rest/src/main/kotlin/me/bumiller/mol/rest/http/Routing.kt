package me.bumiller.mol.rest.http

import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Will set up the applications endpoints.
 *
 * @param basePath The base path
 */
internal fun Application.restRouting(basePath: String) = routing {
    route(basePath) {

    }
}