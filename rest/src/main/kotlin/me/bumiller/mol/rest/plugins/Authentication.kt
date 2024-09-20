package me.bumiller.mol.rest.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.User
import me.bumiller.mol.model.config.AppConfig
import org.koin.ktor.ext.inject

/* Endpoints that can be accessed without having the email verified.
 * POST to /auth/signup/email-verify/ is required to let a user verify their email-address.
 */
private val allowedWithoutEmailVerified = mapOf(
    HttpMethod.Post to "auth/signup/email-verify/"
)

/* Endpoints that can be accessed without having the profile set up.
 * POST to /user/profile/ is required to let a user set up their profile for the first time.
 */
private val allowedWithoutProfileSet = mapOf(
    HttpMethod.Post to "auth/signup/email-verify/",
    HttpMethod.Post to "user/profile/"
)

internal fun Application.authentication(appConfig: AppConfig, basePath: String) {

    val verifier = JWT
        .require(Algorithm.HMAC256(appConfig.jwtSecret))
        .build()

    val userService by inject<UserService>()

    install(Authentication) {
        jwt {
            verifier(verifier)

            validate { credential ->
                val user = try {
                    userService.getSpecific(email = credential.subject, onlyActive = false)
                } catch (e: ServiceException.UserNotFound) {
                    null
                }

                val noUserFound = user == null
                val userNoProfile = user?.profile == null
                val emailVerified = user?.isEmailVerified == true
                val whitelistedForEmail = allowedWithoutEmailVerified.any { entry ->
                    request.httpMethod == entry.key && request.uri.endsWith(
                        "$basePath${entry.value}"
                    )
                }
                val whitelistedForProfile = allowedWithoutProfileSet.any { entry ->
                    request.httpMethod == entry.key && request.uri.endsWith(
                        "$basePath${entry.value}"
                    )
                }

                val badAuthentication = noUserFound ||
                        (userNoProfile && !whitelistedForProfile) ||
                        (!emailVerified && !whitelistedForEmail)

                if (badAuthentication) null
                else UserPrincipalWrapper(user!!)
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