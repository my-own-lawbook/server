package me.bumiller.mol.rest.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.dataconversion.*
import kotlinx.datetime.LocalDate
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.MemberRole

/**
 * Installs the [DataConversion] plugin into the application
 */
internal fun Application.dataConversion() {
    install(DataConversion) {
        localDate()
        gender()
        invitationStatus()
        memberRole()
    }
}

private typealias Config = io.ktor.util.converters.DataConversion.Configuration

private fun Config.invitationStatus() {
    convert<InvitationStatus> {
        decode { values ->
            values.single().let { value ->
                InvitationStatus.entries.first { it.name.lowercase() == value }
            }
        }

        encode { status ->
            listOf(status.name.lowercase())
        }
    }
}

private fun Config.memberRole() {
    convert<MemberRole> {
        decode { values ->
            values.single().let { value ->
                MemberRole.entries.first { it.value == value.toIntOrNull() }
            }
        }

        encode { status ->
            listOf(status.value.toString())
        }
    }
}

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