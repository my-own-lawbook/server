package me.bumiller.mol.rest.validation

import io.ktor.server.application.*
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.http.bad
import org.koin.ktor.ext.inject

/**
 * Scope that the [Validatable.validate] method is invoked in
 */
internal interface ValidationScope {

    /**
     * The [TwoFactorTokenService]
     */
    val tokenService: TwoFactorTokenService

    /**
     * The [UserService]
     */
    val userService: UserService

    /**
     * The [AuthService]
     */
    val authService: AuthService

}

/**
 * Interface for any class that can have their contents validated
 */
internal interface Validatable {

    /**
     * Called when the object will be validated.
     */
    suspend fun ValidationScope.validate() {}

}

/**
 * Calls the [ApplicationCall.receiveOptional] method and automatically validates the [Body].
 */
internal suspend inline fun <reified Body : Validatable> ApplicationCall.validated(): Body {
    return receiveOptional<Body>()?.apply {
        with(application.validationScope) {
            validate()
        }
    } ?: bad()
}

/**
 * Extension function that creates an application wide [ValidationScope]
 */
private val Application.validationScope: ValidationScope
    get() = object : ValidationScope {

        override val tokenService by inject<TwoFactorTokenService>()

        override val userService by inject<UserService>()

        override val authService by inject<AuthService>()

    }
