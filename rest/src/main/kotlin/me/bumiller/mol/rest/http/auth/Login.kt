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
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.http.bad
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.rest.response.user.TokenResponse
import me.bumiller.mol.rest.validation.*
import org.koin.ktor.ext.inject

/**
 * Sets up the routes:
 *
 * - POST /login/: Logging in with credentials
 * - POST /login/refresh/: Logging in with a refresh token
 */
internal fun Route.login() {
    val authService by inject<AuthService>()
    val tokenService by inject<TwoFactorTokenService>()

    route("login/") {
        loginWithCredentials(authService)
        loginWithRefreshToken(tokenService, authService)
    }
}

//
// Request bodies
//

/**
 * Contains the fields required by a request to POST /auth/login/
 */
@Serializable
private data class LoginCredentialsRequest(

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

    override suspend fun ValidationScope.validate() {
        listOf(email, username).map(Optional<*>::isPresent).distinct().size.let { size ->
            if (size == 1) bad("Only either 'email' or 'username' must be passed!")
        }
    }

}

/**
 * Contains the fields required by a request to POST /auth/login/refresh/
 */
@Serializable
private data class LoginRefreshRequest(

    /**
     * The refresh token
     */
    val token: String

) : Validatable {

    override suspend fun ValidationScope.validate() {
        token.validateUUID()
        token.toUUID().validateTwoFactorTokenValid(tokenService, TwoFactorTokenType.RefreshToken)
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
    if (user != null) {
        val tokens = authService.loginUser(user.id)
        call.respond(HttpStatusCode.OK, TokenResponse.create(tokens))
    } else call.respond(HttpStatusCode.Unauthorized)
}

/**
 * Sets up the route to POST /auth/login/refresh/
 *
 * Allows to authenticate using a previously acquired refresh token
 */
private fun Route.loginWithRefreshToken(tokenService: TwoFactorTokenService, authService: AuthService) = post("refresh/") {
    val uuid = call.validated<LoginRefreshRequest>().token.toUUID()
    val token = tokenService.getSpecific(token = uuid) ?: internal()

    tokenService.markAsUsed(token.id)

    val tokens = authService.loginUser(token.user.id)
    call.respond(HttpStatusCode.OK, TokenResponse.create(tokens))
}