package me.bumiller.mol.rest.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import me.bumiller.mol.model.config.AppConfig

internal fun Application.authentication(appConfig: AppConfig) {

    val verifier = JWT
        .require(Algorithm.HMAC256(appConfig.jwtSecret))
        .build()

    install(Authentication) {
        jwt {
            verifier(verifier)

            validate { credential ->
                JWTPrincipal(credential.payload)
            }
        }
    }
}