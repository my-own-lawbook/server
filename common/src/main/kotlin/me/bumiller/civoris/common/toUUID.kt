package me.bumiller.civoris.common

import java.util.*

/**
 * Shorthand extension function for parsing a UUID.
 *
 * @return The UUID
 */
fun String.toUUID(): UUID = UUID.fromString(this)

/**
 * Safely parses a string to a UUID
 *
 * @return The UUID, or null if the string cannot be parsed
 */
fun String.toUUIDSafe(): UUID? = try {
    toUUID()
} catch (e: IllegalArgumentException) {
    null
}