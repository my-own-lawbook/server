package me.bumiller.mol.validation.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.test.util.lawBookModel
import me.bumiller.mol.test.util.lawEntryModel
import me.bumiller.mol.test.util.userModel
import me.bumiller.mol.validation.LawPermission
import me.bumiller.mol.validation.LawResourceScope
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AccessValidatorTest {

    private lateinit var lawContentService: LawContentService
    private lateinit var memberContentService: MemberContentService
    private lateinit var userService: UserService

    private lateinit var accessValidator: ServiceAccessValidator

    @BeforeEach
    fun setup() {
        lawContentService = mockk()
        memberContentService = mockk()
        userService = mockk()

        accessValidator = ServiceAccessValidator(lawContentService, memberContentService, userService)
    }

    private val user = userModel(1L)
    private val book = lawBookModel(1L)
    private val entry = lawEntryModel(1L)
    private val role = MemberRole.Admin

    @Test
    fun `throws 500 if user is not found`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } throws ServiceException.UserNotFound(1L)

        val ex = assertThrows<RequestException> {
            accessValidator.hasAccess(LawResourceScope.Book, LawPermission.Edit, 1L, 1L)
        }
        assertEquals(500, ex.code)
    }

    @Test
    fun `throws 404 resource is not found and is set to throw`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user

        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } throws ServiceException.LawBookNotFound(1L)
        val ex1 = assertThrows<RequestException> {
            accessValidator.hasAccess(LawResourceScope.Book, LawPermission.Edit, 1L, 1L)
        }

        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { lawContentService.getBookByEntry(any()) } throws ServiceException.LawBookNotFound(1L)
        val ex2 = assertThrows<RequestException> {
            accessValidator.hasAccess(LawResourceScope.Entry, LawPermission.Edit, 1L, 1L)
        }

        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { lawContentService.getBookByEntry(any()) } returns book
        coEvery { lawContentService.getEntryForSection(any()) } throws ServiceException.LawEntryNotFound(1L)
        val ex3 = assertThrows<RequestException> {
            accessValidator.hasAccess(LawResourceScope.Section, LawPermission.Edit, 1L, 1L)
        }

        assertEquals(404, ex1.code)
        assertEquals(404, ex2.code)
        assertEquals(404, ex3.code)
    }

    @Test
    fun `returns null if resource is not found and is not set to throw`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user

        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } throws ServiceException.LawBookNotFound(1L)
        val returned1 = accessValidator.hasAccess(LawResourceScope.Book, LawPermission.Edit, 1L, 1L, false)

        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { lawContentService.getBookByEntry(any()) } throws ServiceException.LawBookNotFound(1L)
        val returned2 = accessValidator.hasAccess(LawResourceScope.Entry, LawPermission.Edit, 1L, 1L, false)

        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { lawContentService.getBookByEntry(any()) } returns book
        coEvery { lawContentService.getEntryForSection(any()) } throws ServiceException.LawEntryNotFound(1L)
        val returned3 = accessValidator.hasAccess(LawResourceScope.Section, LawPermission.Edit, 1L, 1L, false)

        assertNull(returned1)
        assertNull(returned2)
        assertNull(returned3)
    }

    @Test
    fun `throws 404 if user is not a member of the book and is set to throw`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { memberContentService.getMemberRole(any(), any()) } throws ServiceException.UserNotMemberOfBook(1L, 1L)

        val ex = assertThrows<RequestException> {
            accessValidator.hasAccess(LawResourceScope.Book, LawPermission.Edit, 1L, 1L)
        }
        assertEquals(404, ex.code)

        coVerify(exactly = 1) { memberContentService.getMemberRole(any(), any()) }
    }

    @Test
    fun `returns false if user is not a member of the book and is not set to throw`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { memberContentService.getMemberRole(any(), any()) } throws ServiceException.UserNotMemberOfBook(1L, 1L)

        val returned = accessValidator.hasAccess(LawResourceScope.Book, LawPermission.Edit, 1L, 1L, false)
        assertFalse(returned ?: true)

        coVerify(exactly = 1) { memberContentService.getMemberRole(any(), any()) }
    }

    @Test
    fun `returns true if access should be granted`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { lawContentService.getBookByEntry(any()) } returns book
        coEvery { lawContentService.getEntryForSection(any()) } returns entry

        val dataBook = mapOf(
            MemberRole.Read to LawPermission.Read,
            MemberRole.Update to LawPermission.Read,
            MemberRole.Write to LawPermission.Read,
            MemberRole.Admin to LawPermission.Read,

            MemberRole.Write to LawPermission.Create,
            MemberRole.Admin to LawPermission.Create,

            MemberRole.Admin to LawPermission.Edit
        )

        val dataOther = mapOf(
            MemberRole.Read to LawPermission.Read,
            MemberRole.Update to LawPermission.Read,
            MemberRole.Write to LawPermission.Read,
            MemberRole.Admin to LawPermission.Read,

            MemberRole.Update to LawPermission.Edit,
            MemberRole.Write to LawPermission.Edit,
            MemberRole.Admin to LawPermission.Edit,

            MemberRole.Write to LawPermission.Create,
            MemberRole.Admin to LawPermission.Create,
        )

        for ((role, permission) in dataBook) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val returned = accessValidator.hasAccess(LawResourceScope.Book, permission, 1L, 1L)

            assertTrue(returned ?: false)
        }

        for ((role, permission) in dataOther) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val returned = accessValidator.hasAccess(LawResourceScope.Entry, permission, 1L, 1L)

            assertTrue(returned ?: false)
        }

        for ((role, permission) in dataOther) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val returned = accessValidator.hasAccess(LawResourceScope.Section, permission, 1L, 1L)

            assertTrue(returned ?: false)
        }
    }

    @Test
    fun `returns false if access should not be granted and is not set to throw`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { lawContentService.getBookByEntry(any()) } returns book
        coEvery { lawContentService.getEntryForSection(any()) } returns entry

        val dataBook = mapOf(
            MemberRole.Read to LawPermission.Create,
            MemberRole.Update to LawPermission.Create,

            MemberRole.Read to LawPermission.Edit,
            MemberRole.Update to LawPermission.Edit,
            MemberRole.Write to LawPermission.Edit
        )

        val dataOther = mapOf(
            MemberRole.Read to LawPermission.Create, MemberRole.Update to LawPermission.Create,

            MemberRole.Read to LawPermission.Edit
        )

        for ((role, permission) in dataBook) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val returned = accessValidator.hasAccess(LawResourceScope.Book, permission, 1L, 1L, false)

            assertFalse(returned ?: true)
        }

        for ((role, permission) in dataOther) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val returned = accessValidator.hasAccess(LawResourceScope.Entry, permission, 1L, 1L, false)

            assertFalse(returned ?: true)
        }

        for ((role, permission) in dataOther) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val returned = accessValidator.hasAccess(LawResourceScope.Section, permission, 1L, 1L, false)

            assertFalse(returned ?: true)
        }
    }

    @Test
    fun `throws 404 or 401 false if access should not be granted and is not to throw`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { lawContentService.getBookByEntry(any()) } returns book
        coEvery { lawContentService.getEntryForSection(any()) } returns entry

        val dataBook = mapOf(
            MemberRole.Read to LawPermission.Create,
            MemberRole.Update to LawPermission.Create,

            MemberRole.Read to LawPermission.Edit,
            MemberRole.Update to LawPermission.Edit,
            MemberRole.Write to LawPermission.Edit
        )

        val dataOther = mapOf(
            MemberRole.Read to LawPermission.Create, MemberRole.Update to LawPermission.Create,

            MemberRole.Read to LawPermission.Edit
        )

        for ((role, permission) in dataBook) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val ex = assertThrows<RequestException> {
                accessValidator.hasAccess(LawResourceScope.Book, permission, 1L, 1L)
            }

            assertEquals(401, ex.code)
        }

        for ((role, permission) in dataOther) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val ex = assertThrows<RequestException> {
                accessValidator.hasAccess(LawResourceScope.Entry, permission, 1L, 1L)
            }

            assertEquals(401, ex.code)
        }

        for ((role, permission) in dataOther) {
            coEvery { memberContentService.getMemberRole(any(), any()) } returns role
            val ex = assertThrows<RequestException> {
                accessValidator.hasAccess(LawResourceScope.Section, permission, 1L, 1L)
            }

            assertEquals(401, ex.code)
        }
    }

}