package me.bumiller.mol.rest.http.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.toUUID
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.model.http.notFoundIdentifier
import me.bumiller.mol.rest.response.user.UserWithoutProfileResponse
import me.bumiller.mol.validation.Validatable
import me.bumiller.mol.validation.ValidationScope
import me.bumiller.mol.validation.actions.*
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
    val userService by inject<UserService>()
    val tokenService by inject<TwoFactorTokenService>()

    route("signup/") {
        createUser(authService)
        requestEmailToken(userService, authService)
        submitEmailToken(tokenService, authService, userService)
    }
}

//
// Request bodies
//

/**
 * Contains the fields required by the request to POST /auth/signup/.
 */
@Serializable
private data class CreateUserRequest(

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

    override suspend fun ValidationScope.validate() {
        validateThat(email).isEmail()
        validateThat(username).isUsername()
        validateThat(password).isPassword()
        validateThat(email).isEmailUnique()
        validateThat(username).isUsernameUnique()
    }

}

/**
 * Contains the fields required by the request to POST /auth/signup/email-verify/
 */
@Serializable
private data class RequestEmailTokenRequest(

    /**
     * The email to send the token to
     */
    val email: String

): Validatable {
    override suspend fun ValidationScope.validate() {
        validateThat(email).isEmail()
    }
}

/**
 * Contains the fields required by a request to PATCH /auth/signup/email-verify/
 */
@Serializable
private data class SubmitEmailTokenRequest(

    /**
     * The token that was sent to the email address
     */
    val token: String

): Validatable {

    override suspend fun ValidationScope.validate() {
        validateThat(token).isUUID()
        validateThat(token.toUUID()).isTokenValid(TwoFactorTokenType.EmailConfirm)
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
    call.respond(HttpStatusCode.Created, UserWithoutProfileResponse.create(createdUser))
}

/**
 * Sets up the POST /auth/signup/email-verify/
 *
 * Allows to request an email-verify-token to a specific email address
 */
private fun Route.requestEmailToken(userService: UserService, authService: AuthService) = post("email-verify/") {
    val email = call.validated<RequestEmailTokenRequest>().email

    val user = userService.getSpecific(email = email)
    if(user?.profile != null && !user.isEmailVerified) {
        authService.sendEmailVerification(user)
    }

    call.respond(HttpStatusCode.Accepted)
}

/**
 * Sets up the route PATCH auth/signup/email-verify/
 *
 * Allows to submit an email-verify-token sent to an email address.
 */
private fun Route.submitEmailToken(
    tokenService: TwoFactorTokenService,
    authService: AuthService,
    userService: UserService
) = patch("email-verify/") {
    val tokenUUID = call.validated<SubmitEmailTokenRequest>().token.toUUID()
    val token = tokenService.getSpecific(token = tokenUUID) ?: internal()
    val user = userService.getSpecific(id = token.user.id)

    if (user != null) {
        authService.validateEmailWithToken(token.token)
        call.respond(HttpStatusCode.OK)
    } else notFoundIdentifier("two-factor-token", token.token.toString())
}