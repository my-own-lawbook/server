package me.bumiller.mol

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.bumiller.mol.rest.restApi
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
        restApi()
    }.start(wait = true)
}