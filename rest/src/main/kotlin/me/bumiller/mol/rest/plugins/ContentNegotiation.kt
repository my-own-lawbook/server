package me.bumiller.mol.rest.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

/**
 * Installs the [ContentNegotiation] plugin into the application
 */
internal fun Application.contentNegotiation() {
    install(ContentNegotiation)
}