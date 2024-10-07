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
    val value: T

)

/**
 * Creates a [ValidatableWrapper] for a given [value]
 *
 * @param value The field to validate
 * @return The [ValidatableWrapper] to perform validation checks on
 */
fun <T> validateThat(value: T): ValidatableWrapper<T> =
    ValidatableWrapper(value)

/**
 * Creates a [ValidatableWrapper] for a given optional value.
 *
 * @param value The optional value+
 * @return The [ValidatableWrapper] or null if [value] is not present
 */
fun <T> validateThatOptional(value: Optional<T>): ValidatableWrapper<T>? =
    if (value.isPresent) ValidatableWrapper(value.get()) else null

