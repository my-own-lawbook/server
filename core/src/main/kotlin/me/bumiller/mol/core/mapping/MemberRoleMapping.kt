package me.bumiller.mol.core.mapping

import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref.Roles.*
import me.bumiller.mol.model.MemberRole

internal fun memberRoleFromString(str: String): MemberRole =
    when (str) {
        Admin.serializedName -> MemberRole.Admin
        Update.serializedName -> MemberRole.Moderator
        Read.serializedName -> MemberRole.Member
        else -> error("No member role found for string '$str'")
    }

internal fun memberRoleToString(role: MemberRole): String =
    when (role) {
        MemberRole.Admin -> Admin.serializedName
        MemberRole.Member -> Read.serializedName
        MemberRole.Moderator -> Update.serializedName
    }