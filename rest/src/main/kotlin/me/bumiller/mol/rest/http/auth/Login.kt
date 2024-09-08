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
import me.bumiller.mol.rest.http.response.user.TokenResponse
import me.bumiller.mol.rest.validation.receiveOptional
import me.bumiller.mol.rest.validation.validateTwoFactorTokenValid
import me.bumiller.mol.rest.validation.validateUUID
import org.koin.ktor.ext.inject

/**
 * Sets up the routes:
 *
 * POST /login/: Logging in with credentials
 * POST /login/refresh/: Logging in with a refresh token
 */
internal fun Route.login() {
    val authService by inject<AuthService>()
    val tokenService by inject<TwoFactorTokenService>()

    route("login/") {
        loginWithCredentials(authService)
        loginWithRefreshToken(tokenService, authService)
    }
}

private suspend inline fun <reified Body : Any> ApplicationCall.validated(): Body {
    val tokenService by inject<TwoFactorTokenService>()

    return receiveOptional<Body>()?.apply {
        when (this) {
            is LoginCredentialsRequest -> validate()
            is LoginRefreshRequest -> validate(tokenService)
        }
    } ?: bad()
}

@Serializable
private data class LoginCredentialsRequest(

    val email: Optional<String> = empty(),

    val username: Optional<String> = empty(),

    val password: String

) {

    fun validate() {
        listOf(email, username).map(Optional<*>::isPresent).distinct().size.let { size ->
            if (size == 1) bad("Only either 'email' or 'username' must be passed!")
        }
    }

}

private fun Route.loginWithCredentials(authService: AuthService) = post {
    val body = call.validated<LoginCredentialsRequest>()

    val user = authService.getAuthenticatedUser(body.email.getOrNull(), body.username.getOrNull(), body.password)
    if (user != null) {
        val tokens = authService.loginUser(user.id)
        call.respond(HttpStatusCode.OK, TokenResponse.create(tokens))
    } else call.respond(HttpStatusCode.Unauthorized)
}

@Serializable
private data class LoginRefreshRequest(

    val token: String

) {

    suspend fun validate(tokenService: TwoFactorTokenService) {
        token.validateUUID()
        token.toUUID().validateTwoFactorTokenValid(tokenService, TwoFactorTokenType.RefreshToken)
    }

}

private fun Route.loginWithRefreshToken(tokenService: TwoFactorTokenService, authService: AuthService) = post("refresh/") {
    val uuid = call.validated<LoginRefreshRequest>().token.toUUID()
    val token = tokenService.getSpecific(token = uuid)!!

    tokenService.markAsUsed(token.id)

    val tokens = authService.loginUser(token.user.id)
    call.respond(HttpStatusCode.OK, TokenResponse.create(tokens))
}