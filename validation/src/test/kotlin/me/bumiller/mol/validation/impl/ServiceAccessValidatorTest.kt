package me.bumiller.mol.validation.impl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.model.satisfies
import me.bumiller.mol.test.util.lawBookModel
import me.bumiller.mol.test.util.lawEntryModel
import me.bumiller.mol.test.util.lawSectionModel
import me.bumiller.mol.test.util.userModel
import me.bumiller.mol.validation.AccessValidator
import me.bumiller.mol.validation.ScopedPermission
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ServiceAccessValidatorTest {

    private lateinit var lawContentService: LawContentService
    private lateinit var memberContentService: MemberContentService
    private lateinit var userService: UserService

    private lateinit var accessValidator: AccessValidator

    @BeforeEach
    fun setup() {
        lawContentService = mockk()
        memberContentService = mockk()
        userService = mockk()

        accessValidator = ServiceAccessValidator(lawContentService, memberContentService, userService)
    }

    @Test
    fun `resolveScoped throws 500 if user is not found`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any(), any()) } throws ServiceException.UserNotFound(1L)

        val ex1 = assertThrows<RequestException> {
            accessValidator.resolveScoped(ScopedPermission.Books.Write(1L), 1L, true)
        }
        val ex2 = assertThrows<RequestException> {
            accessValidator.resolveScoped(ScopedPermission.Books.Write(1L), 1L, false)
        }

        assertEquals(500, ex1.code)
        assertEquals(500, ex2.code)
    }

    private val user = userModel(1L)
    private val book = lawBookModel(1L)
    private val entry = lawEntryModel(1L)
    private val section = lawSectionModel(1L)

    @Test
    fun `resolveScoped for book permission throws 404 if book is not found`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any(), any()) } returns user
        coEvery { lawContentService.getSpecificBook(3L) } throws ServiceException.LawBookNotFound(3L)

        val ex = assertThrows<RequestException> {
            accessValidator.resolveScoped(ScopedPermission.Books.Write(3L), 1L, true)
        }
        assertEquals(404, ex.code)
    }

    @Test
    fun `resolveScoped for entry permission throws 404 if entry is not found`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any(), any()) } returns user
        coEvery { lawContentService.getBookByEntry(3L) } throws ServiceException.LawEntryNotFound(3L)

        val ex = assertThrows<RequestException> {
            accessValidator.resolveScoped(ScopedPermission.Entries.Write(3L), 1L, true)
        }
        assertEquals(404, ex.code)
    }

    @Test
    fun `resolveScoped for section permission throws 404 if section is not found`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any(), any()) } returns user
        coEvery { lawContentService.getEntryForSection(3L) } throws ServiceException.LawSectionNotFound(3L)

        val ex = assertThrows<RequestException> {
            accessValidator.resolveScoped(ScopedPermission.Sections.Write(3L), 1L, true)
        }
        assertEquals(404, ex.code)
    }

    private fun minRoleForLawPermission(permission: ScopedPermission) =
        when (permission) {
            is ScopedPermission.Books.Children.Create -> MemberRole.Moderator
            is ScopedPermission.Books.Children.Read -> MemberRole.Member
            is ScopedPermission.Books.Members.ManageInvitations -> MemberRole.Admin
            is ScopedPermission.Books.Members.Read -> MemberRole.Member
            is ScopedPermission.Books.Members.ReadInvitations -> MemberRole.Moderator
            is ScopedPermission.Books.Members.Remove -> MemberRole.Admin
            is ScopedPermission.Books.Read -> null
            is ScopedPermission.Books.Write -> MemberRole.Admin
            is ScopedPermission.Entries.Children.Create -> MemberRole.Moderator
            is ScopedPermission.Entries.Children.Read -> MemberRole.Member
            is ScopedPermission.Entries.Read -> MemberRole.Member
            is ScopedPermission.Entries.Write -> MemberRole.Moderator
            is ScopedPermission.Sections.Read -> MemberRole.Member
            is ScopedPermission.Sections.Write -> MemberRole.Moderator
            else -> error("")
        }

    @ParameterizedTest(name = "resolveScoped for law permission throws 404 or returns false if minimum permission is not met for permission {0}")
    @MethodSource("lawPermissions")
    fun `resolveScoped for law permission throws 404 or returns false if minimum permission is not met`(permission: ScopedPermission) =
        runTest {
            coEvery { lawContentService.getSpecificBook(book.id) } returns book
            coEvery { lawContentService.getBookByEntry(entry.id) } returns book
            coEvery { lawContentService.getEntryForSection(section.id) } returns entry
            coEvery { userService.getSpecific(user.id) } returns user

            MemberRole.entries.forEach { memberRole ->
                coEvery { memberContentService.getMemberRole(any(), any()) } returns memberRole

                val returned = accessValidator.resolveScoped(permission, user.id, false)

                val minRole = minRoleForLawPermission(permission)
                val shouldBeGranted = minRole?.let { memberRole satisfies it } ?: true

                if (shouldBeGranted) {
                    assertTrue(returned)
                    assertDoesNotThrow {
                        accessValidator.resolveScoped(permission, user.id, true)
                    }
                } else {
                    assertFalse(returned)
                    val ex = assertThrows<RequestException> {
                        accessValidator.resolveScoped(permission, user.id, true)
                    }
                    assertEquals(404, ex.code)
                }
            }
        }

    @Test
    fun `resolveScoped for ScopedPermission#Users#Read throws 404 or returns false if email is required`() = runTest {
        coEvery { userService.getSpecific(any()) } returns user

        val returned1 = accessValidator.resolveScoped(ScopedPermission.Users.Read(email = true, id = 1L), 1L, false)
        val returned2 = accessValidator.resolveScoped(ScopedPermission.Users.Read(email = false, id = 1L), 1L, false)

        assertFalse(returned1)
        assertTrue(returned2)

        val ex1 = assertThrows<RequestException> {
            accessValidator.resolveScoped(ScopedPermission.Users.Read(email = true, id = 1L), 1L, true)
        }
        assertEquals(404, ex1.code)

        assertDoesNotThrow {
            accessValidator.resolveScoped(ScopedPermission.Users.Read(email = false, id = 1L), 1L, true)
        }
    }


    companion object {

        @JvmStatic
        fun lawPermissions(): List<ScopedPermission> = listOf(
            ScopedPermission.Books.Children.Create(1L),
            ScopedPermission.Books.Children.Read(1L),
            ScopedPermission.Books.Members.ManageInvitations(1L),
            ScopedPermission.Books.Members.Read(1L),
            ScopedPermission.Books.Members.ReadInvitations(1L),
            ScopedPermission.Books.Members.Remove(1L),
            ScopedPermission.Books.Read(1L),
            ScopedPermission.Books.Write(1L),
            ScopedPermission.Entries.Children.Create(1L),
            ScopedPermission.Entries.Children.Read(1L),
            ScopedPermission.Entries.Read(1L),
            ScopedPermission.Entries.Write(1L),
            ScopedPermission.Sections.Read(1L),
            ScopedPermission.Sections.Write(1L)
        )

    }

}