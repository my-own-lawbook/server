package me.bumiller.mol

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import me.bumiller.mol.database.di.databaseModule
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.plugin.Koin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger(Application::class.java)

/**
 * Main entrypoint into the application which will start the server
 */
fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0"
    ) {
        plugins()
        initDb()
    }.start(wait = true)
}

private fun Application.plugins() {
    install(ContentNegotiation) {
        json()
    }
    install(Koin) {
        modules(databaseModule)
    }
}

private fun initDb() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/mol_db",
        user = "admin",
        password = "admin"
    )
}