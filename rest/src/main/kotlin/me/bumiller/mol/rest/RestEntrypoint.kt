package me.bumiller.mol.rest

import io.ktor.server.application.*
import me.bumiller.mol.rest.http.restRouting
import me.bumiller.mol.rest.plugins.contentNegotiation
import me.bumiller.mol.rest.plugins.koin
import me.bumiller.mol.rest.startup.initDatabase

/**
 * Main entrypoint for the REST-API. This will set up the endpoints.
 *
 * @param basePath The base path that will contain the endpoints. Defaults to '/api/v1'
 */
fun Application.restApi(basePath: String = "/api/v1") {
    setupPlugins()
    initDatabase()
    restRouting(basePath)
}

private fun Application.setupPlugins() {
    contentNegotiation()
    koin()
}