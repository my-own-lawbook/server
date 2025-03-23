package me.bumiller.civoris.email.formatting

import kotlinx.datetime.Instant
import me.bumiller.civoris.model.MemberRole
import me.bumiller.civoris.model.User

internal fun User.formatFullName() = profile?.run {
    "$firstName $lastName"
} ?: throw IllegalStateException("Cannot format the name of a user with no profile set: '$this'.")

internal fun MemberRole.formatName() = when (this) {
    me.bumiller.civoris.model.MemberRole.Admin -> "Admin"
    me.bumiller.civoris.model.MemberRole.Moderator -> "Moderator"
    me.bumiller.civoris.model.MemberRole.Member -> "Member"
}

internal fun Instant.format() = toString()