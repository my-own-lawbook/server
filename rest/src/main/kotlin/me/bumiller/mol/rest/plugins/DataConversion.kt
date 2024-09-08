package me.bumiller.mol.rest.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.dataconversion.*
import kotlinx.datetime.LocalDate
import me.bumiller.mol.model.Gender

/**
 * Installs the [DataConversion] plugin into the application
 */
internal fun Application.dataConversion() {
    install(DataConversion) {
        localDate()
        gender()
    }
}

private typealias Config = io.ktor.util.converters.DataConversion.Configuration

private fun Config.localDate() {
    convert<LocalDate> {
        decode { values ->
            LocalDate.parse(values.single())
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
                else -> throw IllegalStateException("Did not find a matching gender for string '${values.single()}'")
            }
        }

        encode { gender ->
            listOf(gender.serializedName)
        }
    }
}