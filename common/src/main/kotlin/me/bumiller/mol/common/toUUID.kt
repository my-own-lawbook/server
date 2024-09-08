package me.bumiller.mol.common

import java.util.UUID

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