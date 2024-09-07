package me.bumiller.mol.rest.plugins

import io.ktor.server.application.*
import me.bumiller.mol.core.di.dataServiceModule
import me.bumiller.mol.core.di.servicesModule
import org.koin.ktor.plugin.Koin

/**
 * Installs the [Koin] module into the application.
 *
 * Sets up the module dependencies
 */
internal fun Application.koin() {
    install(Koin) {
        modules(dataServiceModule)
        modules(servicesModule)
    }
}