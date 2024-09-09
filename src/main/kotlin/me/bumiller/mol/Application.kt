package me.bumiller.mol

import io.ktor.server.application.*
import io.ktor.server.netty.*
import me.bumiller.mol.model.config.AppConfig
import me.bumiller.mol.rest.restApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val logger: Logger = LoggerFactory.getLogger(Application::class.java)

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.restApi() {
    restApi(jwtConfig())
}

private fun Application.jwtConfig(): AppConfig = environment.config.run {
    AppConfig(
        jwtSecret = property("mol.security.jwt.secret").getString(),
        jwtDuration = property("mol.security.jwt.duration").getString().toLong().minutes,
        refreshDuration = property("mol.security.refresh.token.duration").getString().toLong().seconds,
        emailTokenDuration = property("mol.security.email.token.duration").getString().toLong().seconds,
        databaseUrl = property("mol.database.url").getString(),
        databaseUser = property("mol.database.user").getString(),
        databasePassword = property("mol.database.password").getString()
    )
}