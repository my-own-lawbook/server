package me.bumiller.mol.rest.http

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import me.bumiller.mol.rest.http.auth.login
import me.bumiller.mol.rest.http.auth.signup
import me.bumiller.mol.rest.http.law.bookInvitations
import me.bumiller.mol.rest.http.law.lawBooks
import me.bumiller.mol.rest.http.law.lawEntries
import me.bumiller.mol.rest.http.law.lawSections
import me.bumiller.mol.rest.http.user.profile
import me.bumiller.mol.rest.http.user.userBundled
import me.bumiller.mol.rest.http.users.users

/**
 * Will set up the applications endpoints.
 *
 * @param basePath The base path
 */
internal fun Application.restRouting(basePath: String) = routing {
    route("/$basePath/") {

        authenticate {
            lawBooks()
            bookInvitations()

            lawEntries()
            lawSections()

            users()
            profile()
            userBundled()
        }

        route("auth/") {
            signup()
            login()
        }

    }
}