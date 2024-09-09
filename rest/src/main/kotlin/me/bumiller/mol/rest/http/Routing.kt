package me.bumiller.mol.rest.http

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.rest.http.auth.login
import me.bumiller.mol.rest.http.auth.signup
import me.bumiller.mol.rest.http.user.profile

/**
 * Will set up the applications endpoints.
 *
 * @param basePath The base path
 */
internal fun Application.restRouting(basePath: String) = routing {
    route(basePath) {

        route("auth/") {
            signup()
            login()
        }

        authenticate {
            route("user/") {
                profile()
            }
        }

    }
}