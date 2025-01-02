package me.bumiller.mol.rest.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Sets up the /ping/ route to test connection to the server.
 */
internal fun Route.ping() = get("ping/") {
    call.respond(HttpStatusCode.OK)
}