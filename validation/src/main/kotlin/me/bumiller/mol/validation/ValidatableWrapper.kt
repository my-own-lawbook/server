package me.bumiller.mol.validation

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import me.bumiller.mol.common.Optional

/**
 * Class that wraps a single value (usually a field of a [Validatable]) together with the [scope] in which the object is being validated
 */
data class ValidatableWrapper<T>(

    /**
     * The value of the field to validate
     */
    val value: T,

    /**
     * The validation scope it is being validated in
     */
    val scope: ValidationScope

)

/**
 * Creates a [ValidatableWrapper] for a given [value]
 *
 * @param value The field to validate
 * @return The [ValidatableWrapper] to perform validation checks on
 */
fun <T> ValidationScope.validateThat(value: T): ValidatableWrapper<T> =
    ValidatableWrapper(value, this)

/**
 * Creates a [ValidatableWrapper] for a given [value]
 *
 * @param value The field to validate
 * @return The [ValidatableWrapper] to perform validation checks on
 */
fun <T> PipelineContext<*, ApplicationCall>.validateThat(value: T): ValidatableWrapper<T> =
    ValidatableWrapper(value, this.application.validationScope)

/**
 * Creates a [ValidatableWrapper] for a given value wrapped in an [Optional].
 *
 * @param value The optional for the field to validate
 * @return The [ValidatableWrapper] to perform validation checks on, or null if the optional is not present
 */
fun <T> ValidationScope.validateThatOptional(value: Optional<T>): ValidatableWrapper<T>? =
    if (!value.isPresent) null
    else ValidatableWrapper(value.get(), this)

/**
 * Creates a [ValidatableWrapper] for a given value wrapped in an [Optional].
 *
 * @param value The optional for the field to validate
 * @return The [ValidatableWrapper] to perform validation checks on, or null if the optional is not present
 */
fun <T> PipelineContext<*, ApplicationCall>.validateThatOptional(value: Optional<T>): ValidatableWrapper<T>? =
    if (!value.isPresent) null
    else ValidatableWrapper(value.get(), this.application.validationScope)

