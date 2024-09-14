package me.bumiller.mol.validation.actions

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.bumiller.mol.model.http.badFormat
import me.bumiller.mol.validation.ValidatableWrapper

/**
 * Validates that a given [LocalDate] is in the past (i.e. not today or later)
 */
fun ValidatableWrapper<LocalDate>.isInPast() {
    val utcLocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    val utcLocalDate = LocalDate(utcLocalDateTime.year, utcLocalDateTime.month, utcLocalDateTime.dayOfMonth)
    val isFuture = utcLocalDate >= value

    if (isFuture) badFormat("'date in past'", toString())
}