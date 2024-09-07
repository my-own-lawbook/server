package me.bumiller.mol.rest.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import me.bumiller.mol.model.http.RequestException

/**
 * Sets up the handling of certain exceptions
 */
internal fun Application.exceptionHandling() {
    install(StatusPages) {
        exception<RequestException> { call, cause ->
            val httpStatus = statusCodeFor(cause.code)
            call.respond(httpStatus, cause.body.toString())
        }
    }
}

private fun statusCodeFor(status: Int) = HttpStatusCode.allStatusCodes.find { it.value == status }!!