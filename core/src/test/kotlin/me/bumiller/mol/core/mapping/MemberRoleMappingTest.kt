package me.bumiller.mol.core.mapping

import me.bumiller.mol.database.table.crossref.LawBookMembersCrossref.Roles.*
import me.bumiller.mol.model.MemberRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MemberRoleMappingTest {

    @Test
    fun `memberRoleFromString correctly maps string to role`() {
        val role1 = memberRoleFromString(Write.serializedName)
        val role2 = memberRoleFromString(Admin.serializedName)
        val role3 = memberRoleFromString(Read.serializedName)
        val role4 = memberRoleFromString(Update.serializedName)

        assertEquals(MemberRole.Admin, role2)
        assertEquals(MemberRole.Read, role3)
        assertEquals(MemberRole.Write, role1)
        assertEquals(MemberRole.Update, role4)
    }

    @Test
    fun `memberRoleToString correctly maps role to string`() {
        val role1 = memberRoleToString(MemberRole.Write)
        val role2 = memberRoleToString(MemberRole.Admin)
        val role3 = memberRoleToString(MemberRole.Read)
        val role4 = memberRoleToString(MemberRole.Update)

        assertEquals(Admin.serializedName, role2)
        assertEquals(Read.serializedName, role3)
        assertEquals(Write.serializedName, role1)
        assertEquals(Update.serializedName, role4)
    }

}