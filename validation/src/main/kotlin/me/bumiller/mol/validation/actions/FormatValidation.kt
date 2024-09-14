package me.bumiller.mol.validation.actions

import io.ktor.server.plugins.requestvalidation.*
import me.bumiller.mol.common.toUUIDSafe
import me.bumiller.mol.model.http.badFormat
import me.bumiller.mol.validation.ValidatableWrapper
import java.util.regex.Pattern

private val EmailRegex = Pattern.compile(
    """[a-zA-Z0-9+._%\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\-]{0,64}(\.[a-zA-Z0-9][a-zA-Z0-9\-]{0,25})+"""
)

/**
 * Validates whether an email is a correct format
 *
 * @return The [ValidationResult]
 */
fun ValidatableWrapper<String>.isEmail() =
    if (!EmailRegex.matcher(value).matches()) badFormat("email", value)
    else null

/**
 * Checks for:
 * - Length between 8 and 20
 * - Allows Uppercase/lowercase letters
 * - Allows Numbers
 * - Allows dashes/underscores
 */
private val UsernameRegex = Pattern.compile("^[a-zA-Z0-9-_]{8,16}\$")

/**
 * Validates whether a username is a correct format
 *
 * @return The [ValidationResult]
 */
fun ValidatableWrapper<String>.isUsername() =
    if (!UsernameRegex.matcher(value).matches()) badFormat("username", value)
    else null

private val PasswordRegex = Pattern.compile(
    "^.{6,}$"
)

/**
 * Validates whether a password is a correct format
 *
 * @return The [ValidationResult]
 */
fun ValidatableWrapper<String>.isPassword() =
    if (!PasswordRegex.matcher(value).matches()) badFormat("password", value)
    else null

/**
 * Validates whether a string is a uuid
 */
fun ValidatableWrapper<String>.isUUID() =
    if (value.toUUIDSafe() == null) badFormat("uuid", value)
    else null

private val ProfileNameRegex = Pattern.compile(
    "^[a-zA-Z ]{2,}$"
)

/**
 * Validates whether a string is a uuid
 */
fun ValidatableWrapper<String>.isProfileName() =
    if (!ProfileNameRegex.matcher(value).matches()) badFormat("name", value)
    else null