package me.bumiller.mol.rest.plugins

import io.ktor.server.application.*
import me.bumiller.mol.core.di.dataServiceModule
import me.bumiller.mol.core.di.servicesModule
import me.bumiller.mol.database.di.databaseModule
import me.bumiller.mol.email.di.emailModule
import me.bumiller.mol.model.config.AppConfig
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

/**
 * Installs the [Koin] module into the application.
 *
 * Sets up the module dependencies
 */
internal fun Application.koin(appConfig: AppConfig) {
    install(Koin) {
        val jwtModule = module {
            single { appConfig }
        }
        
        modules(jwtModule)
        modules(databaseModule)
        modules(emailModule)
        modules(dataServiceModule)
        modules(servicesModule)
    }
}