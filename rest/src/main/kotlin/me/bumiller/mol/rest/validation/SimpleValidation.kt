package me.bumiller.mol.rest.validation

import io.ktor.server.plugins.requestvalidation.*
import me.bumiller.mol.model.http.badFormat
import java.util.regex.Pattern

private val EmailRegex = Pattern.compile(
    "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

/**
 * Validates whether an email is a correct format
 *
 * @return The [ValidationResult]
 */
internal fun String.validateEmail() =
    if (!EmailRegex.matcher(this).matches()) badFormat("email", this)
    else null

private val UsernameRegex = Pattern.compile(
    "^(?=[a-zA-Z0-9._]{8,20}$)(?!.*[_.]{2})[^_.].*[^_.]$"
)

/**
 * Validates whether a username is a correct format
 *
 * @return The [ValidationResult]
 */
internal fun String.validateUsername() =
    if (UsernameRegex.matcher(this).matches()) badFormat("username", this)
    else null

private val PasswordRegex = Pattern.compile(
    "^.{6,}$"
)

/**
 * Validates whether a password is a correct format
 *
 * @return The [ValidationResult]
 */
internal fun String.validatePassword() =
    if (UsernameRegex.matcher(this).matches()) badFormat("password", this)
    else null