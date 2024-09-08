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
import me.bumiller.mol.model.http.bad
import me.bumiller.mol.model.http.notFoundIdentifier
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
    val userService by inject<UserService>()
    val tokenService by inject<TwoFactorTokenService>()

    route("/signup/") {
        createUser(authService)
        requestEmailToken(userService, authService)
        submitEmailToken(tokenService, authService, userService)
    }
}

/**
 * Configures the request validation for the signup routes
 */
private suspend inline fun <reified Body : Any> ApplicationCall.validated(): Body {
    val userService by application.inject<UserService>()
    val twoFactorTokenService by application.inject<TwoFactorTokenService>()

    return receiveOptional<Body>()?.apply {
        when (this) {
            is CreateUserRequest -> {
                email.validateEmail()
                username.validateUsername()
                password.validatePassword()
                email.validateEmailUnique(userService)
                username.validateUsernameUnique(userService)
            }
            is RequestEmailTokenRequest -> {
                email.validateEmail()
            }
            is SubmitEmailTokenRequest -> {
                token.validateUUID()
                token.toUUID().validateTwoFactorTokenValid(twoFactorTokenService, TwoFactorTokenType.EmailConfirm)
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

private fun Route.createUser(authService: AuthService) = post {
    val body = call.validated<CreateUserRequest>()

    val createdUser = authService.createNewUser(body.email, body.username, body.password)
    call.respond(HttpStatusCode.Created, UserWithoutProfileResponse.create(createdUser))
}

@Serializable
private data class RequestEmailTokenRequest(
    val email: String
)

private fun Route.requestEmailToken(userService: UserService, authService: AuthService) = post("email-verify/") {
    val email = call.validated<RequestEmailTokenRequest>().email

    val user = userService.getSpecific(email = email)
    if(user?.profile != null && !user.isEmailVerified) {
        authService.sendEmailVerification(user)
    }

    call.respond(HttpStatusCode.Accepted)
}

@Serializable
private data class SubmitEmailTokenRequest(
    val token: String
)

private fun Route.submitEmailToken(
    tokenService: TwoFactorTokenService,
    authService: AuthService,
    userService: UserService
) = patch("email-verify/") {
    val tokenUUID = call.validated<SubmitEmailTokenRequest>().token.toUUID()
    val token = tokenService.getSpecific(token = tokenUUID)!!
    val user = userService.getSpecific(id = token.user.id)

    if (user != null) {
        authService.validateEmailWithToken(token.token)
        call.respond(HttpStatusCode.OK)
    } else notFoundIdentifier("two-factor-token", token.token.toString())
}