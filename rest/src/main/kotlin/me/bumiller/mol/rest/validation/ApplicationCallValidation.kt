package me.bumiller.mol.rest.validation

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import me.bumiller.mol.common.Optional
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
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

    /**
     * The [LawService]
     */
    val lawService: LawService

    /**
     * The [LawContentService]
     */
    val lawContentService: LawContentService

}

/**
 * Interface for any class that can have their contents validated
 */
internal interface Validatable {

    /**
     * Called when the object will be validated.
     */
    suspend fun ValidationScope.validate(){
        // Empty so that implementations can leave this method empty
    }

}

internal data class ValidatableWrapper<T>(val value: T, val scope: ValidationScope)

internal fun <T> ValidationScope.validateThat(value: T): ValidatableWrapper<T> =
    ValidatableWrapper(value, this)

internal fun <T> PipelineContext<*, ApplicationCall>.validateThat(value: T): ValidatableWrapper<T> =
    ValidatableWrapper(value, this.application.validationScope)

internal fun <T> ValidationScope.validateThatOptional(value: Optional<T>): ValidatableWrapper<T>? =
    if(!value.isPresent) null
else ValidatableWrapper(value.get(), this)

internal fun <T> PipelineContext<*, ApplicationCall>.validateThatOptional(value: Optional<T>): ValidatableWrapper<T>? =
    if(!value.isPresent) null
else ValidatableWrapper(value.get(), this.application.validationScope)

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
internal val Application.validationScope: ValidationScope
    get() = object : ValidationScope {

        override val tokenService by inject<TwoFactorTokenService>()

        override val userService by inject<UserService>()

        override val authService by inject<AuthService>()

        override val lawService by inject<LawService>()

        override val lawContentService by inject<LawContentService>()

    }
