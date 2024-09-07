package me.bumiller.mol.rest.http.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.http.bad
import me.bumiller.mol.rest.http.response.user.UserWithoutProfileResponse
import me.bumiller.mol.rest.validation.*
import org.koin.ktor.ext.inject

/**
 * Sets up the routes:
 *
 * - POST /signup/: Creating a new user
 * - POST /signup/email-verify/: Requesting an email-verify token
 * - PUT /signup/email-verify/: Submitting an email-verify token
 */
internal fun Route.signup() {
    val authService by inject<AuthService>()

    route("/signup/") {
        createUser(authService)
    }
}

/**
 * Configures the request validation for the signup routes
 */
private suspend inline fun <reified Body : Any> ApplicationCall.validated(): Body {
    val userService by application.inject<UserService>()

    return receiveOptional<Body>()?.apply {
        when (this) {
            is CreateUserRequest -> {
                email.validateEmail()
                username.validateUsername()
                password.validatePassword()
                email.validateEmailUnique(userService)
                username.validateUsernameUnique(userService)
            }
        }
    } ?: bad()
}

@Serializable
private data class CreateUserRequest(

    val username: String,

    val email: String,

    val password: String

)

private fun Route.createUser(authService: AuthService) {
    post {
        val body = call.validated<CreateUserRequest>()

        val createdUser = authService.createNewUser(body.email, body.username, body.password)
        call.respond(HttpStatusCode.Created, UserWithoutProfileResponse.create(createdUser))
    }
}