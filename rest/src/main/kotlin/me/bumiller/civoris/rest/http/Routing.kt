package me.bumiller.civoris.rest.http

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import me.bumiller.civoris.model.config.AppConfig
import me.bumiller.civoris.rest.http.auth.login
import me.bumiller.civoris.rest.http.auth.logout
import me.bumiller.civoris.rest.http.auth.signup
import me.bumiller.civoris.rest.http.law.bookInvitations
import me.bumiller.civoris.rest.http.law.lawBooks
import me.bumiller.civoris.rest.http.law.lawEntries
import me.bumiller.civoris.rest.http.law.lawSections
import me.bumiller.civoris.rest.http.user.profile
import me.bumiller.civoris.rest.http.user.userBundled
import me.bumiller.civoris.rest.http.users.users

/**
 * Will set up the applications endpoints.
 *
 * @param appConfig The app configuration
 */
internal fun Application.restRouting(appConfig: AppConfig) = routing {
    route(appConfig.basePath) {
        ping()

        authenticate {
            logout()

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