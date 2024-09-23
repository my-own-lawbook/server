package me.bumiller.mol.rest.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.rest.response.error.handle

/**
 * Sets up the handling of certain exceptions
 */
internal fun Application.exceptionHandling() {
    install(StatusPages) {
        exception<ServiceException> { call, cause ->
            val reqEx = try {
                cause.handle()
            } catch (e: RequestException) {
                e
            }

            val httpStatus = statusCodeFor(reqEx.code)
            call.respond(httpStatus, reqEx.body.toString())
        }

        exception<RequestException> { call, cause ->
            val httpStatus = statusCodeFor(cause.code)
            call.respond(httpStatus, cause.body)
        }
    }
}

private fun statusCodeFor(status: Int) = HttpStatusCode.allStatusCodes.find { it.value == status }!!