package me.bumiller.mol.rest.http.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.toUUID
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.http.bad
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.rest.response.user.TokenResponse
import me.bumiller.mol.validation.Validatable
import me.bumiller.mol.validation.actions.isUUID
import me.bumiller.mol.validation.validateThat
import me.bumiller.mol.validation.validated
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
        validateThat(token).isUUID()
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
    val uuid = call.validated<LoginRefreshRequest>().token.toUUID()

    val tokens = try {
        authService.loginUserWithRefreshToken(uuid)
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    call.respond(HttpStatusCode.OK, TokenResponse.create(tokens))
}