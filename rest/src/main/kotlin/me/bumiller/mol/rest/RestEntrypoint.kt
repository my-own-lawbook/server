package me.bumiller.mol.rest

import io.ktor.server.application.*
import me.bumiller.mol.model.config.AppConfig
import me.bumiller.mol.rest.http.restRouting
import me.bumiller.mol.rest.plugins.*
import me.bumiller.mol.rest.plugins.authentication
import me.bumiller.mol.rest.plugins.contentNegotiation
import me.bumiller.mol.rest.plugins.dataConversion
import me.bumiller.mol.rest.plugins.exceptionHandling

/**
 * Main entrypoint for the REST-API. This will set up the endpoints.
 *
 * @param basePath The base path that will contain the endpoints. Defaults to '/api/v1'
 */
fun Application.restApi(appConfig: AppConfig, basePath: String = "/api/v1/") {
    setupPlugins(appConfig, basePath)
    restRouting(basePath)
}

private fun Application.setupPlugins(appConfig: AppConfig, basePath: String) {
    cors()
    contentNegotiation()
    exceptionHandling()
    dataConversion()
    authentication(appConfig, basePath)
}