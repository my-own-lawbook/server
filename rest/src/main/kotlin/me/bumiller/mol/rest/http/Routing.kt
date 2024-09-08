package me.bumiller.mol.rest.http

import io.ktor.server.application.*
import io.ktor.server.routing.*
import me.bumiller.mol.rest.http.auth.login
import me.bumiller.mol.rest.http.auth.signup

/**
 * Will set up the applications endpoints.
 *
 * @param basePath The base path
 */
internal fun Application.restRouting(basePath: String) = routing {
    route("/$basePath/") {
        route("auth/") {
            signup()
            login()
        }
    }
}