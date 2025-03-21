package me.bumiller.civoris.rest.http.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.civoris.common.Optional
import me.bumiller.civoris.common.empty
import me.bumiller.civoris.core.AuthService
import me.bumiller.civoris.core.exception.ServiceException
import me.bumiller.civoris.model.http.bad
import me.bumiller.civoris.model.http.internal
import me.bumiller.civoris.rest.response.user.TokenResponse
import me.bumiller.civoris.validation.Validatable
import me.bumiller.civoris.validation.validated
import org.koin.ktor.ext.inject

/**
 * Sets up the routes:
 *
 * - POST /login/: Logging in with credentials
 * - POST /login/refresh/: Logging in with a refresh token
 */
internal fun Route.login() {
    val authService by inject<AuthService>()

    route("login/") {
        loginWithCredentials(authService)
        loginWithRefreshToken(authService)
    }
}

//
// Request bodies
//

/**
 * Contains the fields required by a request to POST /auth/login/
 */
@Serializable
internal data class LoginCredentialsRequest(

    /**
     * The email
     */
    val email: Optional<String> = empty(),

    /**
     * The username
     */
    val username: Optional<String> = empty(),

    /**
     * The password
     */
    val password: String

) : Validatable {

    override suspend fun validate() {
        listOf(email, username).map(Optional<*>::isPresent).distinct().size.let { size ->
            if (size == 1) bad("Only either 'email' or 'username' must be passed!")
        }
    }

}

/**
 * Contains the fields required by a request to POST /auth/login/refresh/
 */
@Serializable
internal data class LoginRefreshRequest(

    /**
     * The refresh token
     */
    val token: String

) : Validatable {

    override suspend fun validate() {

    }

}

//
// Endpoint mappings
//

/**
 * Sets up the route to POST /auth/login/
 *
 * Allows to log in using email/username and password to authenticate.
 */
private fun Route.loginWithCredentials(authService: AuthService) = post {
    val body = call.validated<LoginCredentialsRequest>()

    val user = authService.getAuthenticatedUser(body.email.getOrNull(), body.username.getOrNull(), body.password)
    if (user == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return@post
    }

    val tokens = try {
        authService.loginUser(user.id)
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    call.respond(HttpStatusCode.OK, TokenResponse.create(tokens))
}

/**
 * Sets up the route to POST /auth/login/refresh/
 *
 * Allows to authenticate using a previously acquired refresh token
 */
private fun Route.loginWithRefreshToken(authService: AuthService) = post("refresh/") {
    val token = call.validated<LoginRefreshRequest>().token

    val tokens = try {
        authService.loginUserWithRefreshToken(token)
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    call.respond(HttpStatusCode.OK, TokenResponse.create(tokens))
}