package me.bumiller.mol.rest.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.dataconversion.*
import kotlinx.datetime.LocalDate
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.http.badFormat
import java.util.UUID

/**
 * Installs the [DataConversion] plugin into the application
 */
internal fun Application.dataConversion() {
    install(DataConversion) {
        localDate()
        gender()
        uuid()
    }
}

private typealias Config = io.ktor.util.converters.DataConversion.Configuration

private fun Config.localDate() {
    convert<LocalDate> {
        decode { values ->
            try {
                LocalDate.parse(values.single())
            } catch (e: Exception) {
                badFormat("date", values.single())
            }
        }

        encode { date ->
            listOf(date.toString())
        }
    }
}

private fun Config.gender() {
    convert<Gender> {
        decode { values ->
            when (values.single()) {
                "male" -> Gender.Male
                "female" -> Gender.Female
                "other" -> Gender.Other
                "disclosed" -> Gender.Disclosed
                else -> badFormat("gender", values.single())
            }
        }

        encode { gender ->
            listOf(gender.serializedName)
        }
    }
}

private fun Config.uuid() {
    convert<UUID> {
        decode { values ->
            try {
                UUID.fromString(values.single())
            } catch (e: Exception) {
                badFormat("uuid", values.single())
            }
        }
        encode { uuid ->
            listOf(uuid.toString())
        }
    }
}