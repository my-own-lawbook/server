package me.bumiller.mol.rest.validation

import io.ktor.server.plugins.requestvalidation.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.bumiller.mol.common.toUUIDSafe
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
internal fun ValidatableWrapper<String>.isEmail() =
    if (!EmailRegex.matcher(value).matches()) badFormat("email", value)
    else null

private val UsernameRegex = Pattern.compile(
    "^(?=[a-zA-Z0-9._]{8,20}$)(?!.*[_.]{2})[^_.].*[^_.]$"
)

/**
 * Validates whether a username is a correct format
 *
 * @return The [ValidationResult]
 */
internal fun ValidatableWrapper<String>.isUsername() =
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
internal fun ValidatableWrapper<String>.isPassword() =
    if (!UsernameRegex.matcher(value).matches()) badFormat("password", value)
    else null

/**
 * Validates whether a string is a uuid
 */
internal fun ValidatableWrapper<String>.isUUID() =
    if (value.toUUIDSafe() == null) badFormat("uuid", value)
    else null

private val ProfileNameRegex = Pattern.compile(
    "^[a-zA-Z]{2,}$"
)

/**
 * Validates whether a string is a uuid
 */
internal fun ValidatableWrapper<String>.isProfileName() =
    if (!ProfileNameRegex.matcher(value).matches()) badFormat("name", value)
    else null

internal fun ValidatableWrapper<LocalDate>.isInPast() {
    val utcLocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    val utcLocalDate = LocalDate(utcLocalDateTime.year, utcLocalDateTime.month, utcLocalDateTime.dayOfMonth)
    val isFuture = utcLocalDate >= value

    if(isFuture) badFormat("'date in past'", toString())
}