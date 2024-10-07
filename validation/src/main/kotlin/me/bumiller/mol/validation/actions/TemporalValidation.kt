package me.bumiller.mol.validation.actions

import kotlinx.datetime.*
import me.bumiller.mol.model.http.badFormat
import me.bumiller.mol.validation.ValidatableWrapper

/**
 * Validates that a given [LocalDate] is in the past (i.e. not today or later)
 */
fun ValidatableWrapper<LocalDate>.isInPast() {
    val utcLocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    val utcLocalDate = LocalDate(utcLocalDateTime.year, utcLocalDateTime.month, utcLocalDateTime.dayOfMonth)
    val isFuture = utcLocalDate <= value

    if (isFuture) badFormat("'date in past'", toString())
}

/**
 * Validates that a given [Instant] is in the future
 */
fun ValidatableWrapper<Instant>.isInFuture() {
    val now = Clock.System.now()

    if (value < now) badFormat("'date in future'", toString())
}