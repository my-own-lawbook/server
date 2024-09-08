package me.bumiller.mol.rest.http.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.model.http.bad
import me.bumiller.mol.rest.http.response.user.TokenResponse
import me.bumiller.mol.rest.validation.receiveOptional
import org.koin.ktor.ext.inject

/**
 * Sets up the routes:
 *
 * POST /login/: Logging in with credentials
 * POST /login/refresh/: Logging in with a refresh token
 */
internal fun Route.login() {
    val authService by inject<AuthService>()

    route("login/") {
        loginWithCredentials(authService)
    }
}

private suspend inline fun <reified Body : Any> ApplicationCall.validated(): Body {
    return receiveOptional<Body>()?.apply {
        when (this) {
            is LoginCredentialsRequest -> validate()
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