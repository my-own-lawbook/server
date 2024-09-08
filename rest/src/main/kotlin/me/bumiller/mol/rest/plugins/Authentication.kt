package me.bumiller.mol.rest.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.User
import me.bumiller.mol.model.config.AppConfig
import org.koin.ktor.ext.inject

internal fun Application.authentication(appConfig: AppConfig) {

    val verifier = JWT
        .require(Algorithm.HMAC256(appConfig.jwtSecret))
        .build()

    val userService by inject<UserService>()

    install(Authentication) {
        jwt {
            verifier(verifier)

            validate { credential ->
                val user = userService.getSpecific(email = credential.subject)

                if (user == null || !user.isEmailVerified) null
                else UserPrincipalWrapper(user)
            }
        }
    }
}

/**
 * Wraps a [User] instance to set the principal to a user object.
 *
 * @param user The contained user
 */
internal data class UserPrincipalWrapper(val user: User) : Principal

/**
 * Shorthand for getting the user principal out of an [ApplicationCall]
 *
 * @return The [User]
 */
internal fun ApplicationCall.authenticatedUser() =
    principal<UserPrincipalWrapper>()?.user
        ?: throw IllegalStateException("authenticatedUser() was called for an ApplicationCall with no UserPrincipalWrapper attached!")