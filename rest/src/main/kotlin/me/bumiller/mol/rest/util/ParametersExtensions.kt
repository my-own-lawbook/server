package me.bumiller.mol.rest.util

import io.ktor.http.*
import me.bumiller.mol.model.http.bad

/**
 * Gets the int-value for a specific key or throws a 400 error
 *
 * @param key The key
 * @return The int-value
 */
internal fun Parameters.intOrBadRequest(key: String): Int =
    get(key)?.toIntOrNull() ?: bad("Invalid format for path parameter '$key'")

/**
 * Gets the long-value for a specific key or throws a 400 error
 *
 * @param key The key
 * @return The long-value
 */
internal fun Parameters.longOrBadRequest(key: String): Long =
    get(key)?.toLongOrNull() ?: bad("Invalid format for path parameter '$key'")