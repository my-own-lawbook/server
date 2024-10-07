package me.bumiller.mol.rest.http.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.toUUID
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.http.conflict
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.rest.response.user.AuthUserWithoutProfileResponse
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.Validatable
import me.bumiller.mol.validation.actions.isEmail
import me.bumiller.mol.validation.actions.isPassword
import me.bumiller.mol.validation.actions.isUUID
import me.bumiller.mol.validation.actions.isUsername
import me.bumiller.mol.validation.validateThat
import me.bumiller.mol.validation.validated
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

    route("signup/") {
        createUser(authService)
        submitEmailToken(authService)

        authenticate {
            requestEmailToken(authService)
        }
    }
}

//
// Request bodies
//

/**
 * Contains the fields required by the request to POST /auth/signup/.
 */
@Serializable
internal data class CreateUserRequest(

    /**
     * The username
     */
    val username: String,

    /**
     * The email
     */
    val email: String,

    /**
     * The raw password
     */
    val password: String

): Validatable {

    override suspend fun validate() {
        validateThat(email).isEmail()
        validateThat(username).isUsername()
        validateThat(password).isPassword()
    }

}

/**
 * Contains the fields required by a request to PATCH /auth/signup/email-verify/
 */
@Serializable
internal data class SubmitEmailTokenRequest(

    /**
     * The token that was sent to the email address
     */
    val token: String

): Validatable {

    override suspend fun validate() {
        validateThat(token).isUUID()
    }

}

//
// Endpoint mappings
//

/**
 * Sets up the POST /auth/signup/ endpoint.
 *
 * Allows to create a new user entry.
 */
private fun Route.createUser(authService: AuthService) = post {
    val body = call.validated<CreateUserRequest>()

    val createdUser = authService.createNewUser(body.email, body.username, body.password)

    call.respond(HttpStatusCode.Created, AuthUserWithoutProfileResponse.create(createdUser))
}

/**
 * Sets up the POST /auth/signup/email-verify/
 *
 * Allows to request an email-verify-token to a specific email address
 */
private fun Route.requestEmailToken(authService: AuthService) = post("email-verify/") {
    if (!user.isEmailVerified) {
        authService.sendEmailVerification(user)
    } else conflict("The email address ${user.email} is already verified!")

    call.respond(HttpStatusCode.Accepted)
}

/**
 * Sets up the route PATCH auth/signup/email-verify/
 *
 * Allows to submit an email-verify-token sent to an email address.
 */
private fun Route.submitEmailToken(
    authService: AuthService
) = patch("email-verify/") {
    val token = call.validated<SubmitEmailTokenRequest>().token.toUUID()

    try {
        authService.validateEmailWithToken(token)
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    call.respond(HttpStatusCode.OK)
}