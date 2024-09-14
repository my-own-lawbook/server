package me.bumiller.mol.validation

import io.ktor.server.application.*
import io.ktor.server.request.*
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.http.bad
import org.koin.ktor.ext.inject

/**
 * Extension function on [ApplicationCall] to wrap the parsing of the request-body that is a [Validatable] to also include the validation of that object.
 */
suspend inline fun <reified Body : Validatable> ApplicationCall.validated(): Body {
    return receiveOptional<Body>()?.apply {
        with(application.validationScope) {
            validate()
        }
    } ?: bad()
}

/**
 * Extension function on [ApplicationCall] to return null if the body of a request could not be serialized into the requested format.
 */
suspend inline fun <reified Body : Any> ApplicationCall.receiveOptional(): Body? =
    try {
        receive<Body>()
    } catch (e: ContentTransformationException) {
        null
    }


/**
 * Extension property that creates an application wide [ValidationScope]
 */
val Application.validationScope: ValidationScope
    get() = object : ValidationScope {

        override val tokenService by inject<TwoFactorTokenService>()

        override val userService by inject<UserService>()

        override val authService by inject<AuthService>()

        override val lawService by inject<LawService>()

        override val lawContentService by inject<LawContentService>()

    }