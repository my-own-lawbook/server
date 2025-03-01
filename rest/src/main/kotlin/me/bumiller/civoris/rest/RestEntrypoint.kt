package me.bumiller.civoris.rest

import io.ktor.server.application.*
import me.bumiller.civoris.model.config.AppConfig
import me.bumiller.civoris.rest.http.restRouting
import me.bumiller.civoris.rest.plugins.*

/**
 * Main entrypoint for the REST-API. This will set up the endpoints.
 *
 * @param appConfig The app configuration
 */
fun Application.restApi(appConfig: AppConfig) {
    setupPlugins(appConfig)
    restRouting(appConfig)
}

private fun Application.setupPlugins(appConfig: AppConfig) {
    cors()
    contentNegotiation()
    exceptionHandling()
    dataConversion()
    authentication(appConfig)
}