package me.bumiller.mol.core.mapping

import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref.Roles.*
import me.bumiller.mol.model.MemberRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MemberRoleMappingTest {

    @Test
    fun `memberRoleFromString correctly maps string to role`() {
        val role2 = memberRoleFromString(Admin.serializedName)
        val role3 = memberRoleFromString(Read.serializedName)
        val role4 = memberRoleFromString(Update.serializedName)

        assertEquals(MemberRole.Admin, role2)
        assertEquals(MemberRole.Member, role3)
        assertEquals(MemberRole.Moderator, role4)
    }

    @Test
    fun `memberRoleToString correctly maps role to string`() {
        val role2 = memberRoleToString(MemberRole.Admin)
        val role3 = memberRoleToString(MemberRole.Member)
        val role4 = memberRoleToString(MemberRole.Moderator)

        assertEquals(Admin.serializedName, role2)
        assertEquals(Read.serializedName, role3)
        assertEquals(Update.serializedName, role4)
    }

}