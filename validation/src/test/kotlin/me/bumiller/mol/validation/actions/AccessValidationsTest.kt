package me.bumiller.mol.validation.actions

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.LawBook
import me.bumiller.mol.model.LawEntry
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.validation.ValidationScope
import me.bumiller.mol.validation.validateThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class AccessValidationsTest {

    val lawContentService: LawContentService = mockk()
    val lawService: LawService = mockk()

    val scope = object : ValidationScope {
        override val tokenService: TwoFactorTokenService = mockk()
        override val userService: UserService = mockk()
        override val authService: AuthService = mockk()
        override val lawService: LawService = this@AccessValidationsTest.lawService
        override val lawContentService = this@AccessValidationsTest.lawContentService
    }

    val user1 = User(1L, "email-1", "username-1", "password-1", true, null)
    val user2 = User(2L, "email-2", "username-2", "password-2", true, null)
    val user3 = User(3L, "email-3", "username-3", "password-3", true, null)

    val book = LawBook(1L, "key", "name", "description", user1, listOf(user3))
    val entry = LawEntry(1L, "key", "name")

    @Test
    fun `hasReadAccess throws for no id passed`() = runTest {
        assertThrows<IllegalArgumentException> {
            scope.validateThat(user1).hasReadAccess()
        }
    }

    @Test
    fun `hasReadAccess throws if user has no access to specified book`() = runTest {
        coEvery { lawContentService.getSpecificBook(any()) } returns book

        val ex = assertThrows<RequestException> {
            scope.validateThat(user2).hasReadAccess(lawBookId = 1L)
        }
        assertEquals(404, ex.code)

        assertDoesNotThrow {
            scope.validateThat(user1).hasReadAccess(lawBookId = 1L)
        }
        assertDoesNotThrow {
            scope.validateThat(user3).hasReadAccess(lawBookId = 1L)
        }
    }

    @Test
    fun `hasReadAccess throws if user has no access to specified entry`() = runTest {
        coEvery { lawContentService.getBookByEntry(1L) } returns book
        coEvery { lawService.isUserMemberOfEntry(any(), any()) } answers { c ->
            (c.invocation.args[0] as Long) in listOf(1L, 3L)
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat(user2).hasReadAccess(lawEntryId = 1L)
        }
        assertEquals(404, ex.code)

        assertDoesNotThrow {
            scope.validateThat(user1).hasReadAccess(lawEntryId = 1L)
        }
        assertDoesNotThrow {
            scope.validateThat(user3).hasReadAccess(lawEntryId = 1L)
        }
    }

    @Test
    fun `hasReadAccess throws if user has no access to specified section`() = runTest {
        coEvery { lawContentService.getEntryForSection(1L) } returns entry
        coEvery { lawContentService.getBookByEntry(1L) } returns book
        coEvery { lawService.isUserMemberOfSection(any(), any()) } answers { c ->
            (c.invocation.args[0] as Long) in listOf(1L, 3L)
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat(user2).hasReadAccess(lawSectionId = 1L)
        }
        assertEquals(404, ex.code)

        assertDoesNotThrow {
            scope.validateThat(user1).hasReadAccess(lawSectionId = 1L)
        }
        assertDoesNotThrow {
            scope.validateThat(user3).hasReadAccess(lawSectionId = 1L)
        }
    }

    @Test
    fun `hasWritAccess throws for no id passed`() = runTest {
        assertThrows<IllegalArgumentException> {
            scope.validateThat(user1).hasWriteAccess()
        }
    }

    @Test
    fun `hasWriteAccess throws if user has no access to specified book`() = runTest {
        coEvery { lawContentService.getSpecificBook(any()) } returns book

        val ex = assertThrows<RequestException> {
            scope.validateThat(user2).hasWriteAccess(lawBookId = 1L)
        }
        assertEquals(404, ex.code)
        val ex2 = assertThrows<RequestException> {
            scope.validateThat(user3).hasWriteAccess(lawBookId = 1L)
        }
        assertEquals(404, ex2.code)

        assertDoesNotThrow {
            scope.validateThat(user1).hasWriteAccess(lawBookId = 1L)
        }
    }

    @Test
    fun `hasWriteAccess throws if user has no access to specified entry`() = runTest {
        coEvery { lawContentService.getBookByEntry(1L) } returns book
        coEvery { lawService.isUserMemberOfEntry(any(), any()) } answers { c ->
            (c.invocation.args[0] as Long) in listOf(1L, 3L)
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat(user2).hasWriteAccess(lawEntryId = 1L)
        }
        assertEquals(404, ex.code)
        val ex2 = assertThrows<RequestException> {
            scope.validateThat(user3).hasWriteAccess(lawEntryId = 1L)
        }
        assertEquals(404, ex2.code)

        assertDoesNotThrow {
            scope.validateThat(user1).hasWriteAccess(lawEntryId = 1L)
        }
    }

    @Test
    fun `hasWriteAccess throws if user has no access to specified section`() = runTest {
        coEvery { lawContentService.getEntryForSection(1L) } returns entry
        coEvery { lawContentService.getBookByEntry(1L) } returns book
        coEvery { lawService.isUserMemberOfSection(any(), any()) } answers { c ->
            (c.invocation.args[0] as Long) in listOf(1L, 3L)
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat(user2).hasWriteAccess(lawSectionId = 1L)
        }
        assertEquals(404, ex.code)
        val ex2 = assertThrows<RequestException> {
            scope.validateThat(user3).hasWriteAccess(lawSectionId = 1L)
        }
        assertEquals(404, ex2.code)

        assertDoesNotThrow {
            scope.validateThat(user1).hasWriteAccess(lawSectionId = 1L)
        }
    }

}