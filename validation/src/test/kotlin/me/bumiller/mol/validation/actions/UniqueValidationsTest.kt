package me.bumiller.mol.validation.actions

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import me.bumiller.mol.common.present
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.*
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.validation.ValidationScope
import me.bumiller.mol.validation.validateThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class UniqueValidationsTest {

    val tokenService: TwoFactorTokenService = mockk()
    val userService: UserService = mockk()
    val authService: AuthService = mockk()
    val lawService: LawService = mockk()
    val lawContentService: LawContentService = mockk()

    val scope = object : ValidationScope {
        override val tokenService = this@UniqueValidationsTest.tokenService
        override val userService = this@UniqueValidationsTest.userService
        override val authService = this@UniqueValidationsTest.authService
        override val lawService = this@UniqueValidationsTest.lawService
        override val lawContentService = this@UniqueValidationsTest.lawContentService
    }

    val profile = UserProfile(1L, LocalDate(200, 1, 1), Gender.Other, "first_name", "last_name")
    val user = User(1L, "email", "username", "password", true, profile)
    val book = LawBook(1L, "key", "name", "description", user, emptyList())
    val entry = LawEntry(1L, "key", "name")
    val section = LawSection(1L, "index", "name", "content")

    @Test
    fun `isEmailUnique works`() = runTest {
        coEvery { userService.getSpecific(email = "email1") } returns user
        coEvery { userService.getSpecific(email = "email2") } returns null

        assertDoesNotThrow {
            scope.validateThat("email2").isEmailUnique()
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat("email1").isEmailUnique()
        }
        assertEquals(409, ex.code)
    }

    @Test
    fun `isUsernameUnique works`() = runTest {
        coEvery { userService.getSpecific(username = "username1") } returns user
        coEvery { userService.getSpecific(username = "username2") } returns null

        assertDoesNotThrow {
            scope.validateThat("username2").isUsernameUnique()
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat("username1").isUsernameUnique()
        }
        assertEquals(409, ex.code)
    }

    @Test
    fun `isUniqueBookKey works`() = runTest {
        coEvery { lawContentService.getSpecificBook(key = "key1") } returns book
        coEvery { lawContentService.getSpecificBook(key = "key2") } returns null

        assertDoesNotThrow {
            scope.validateThat("key2").isUniqueBookKey()
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat("key1").isUniqueBookKey()
        }
        assertEquals(409, ex.code)
    }


    @Test
    fun `isUniqueEntryKey works`() = runTest {
        coEvery { lawContentService.getSpecificEntry(key = present("key1"), parentBookId = present(8L)) } returns entry
        coEvery { lawContentService.getSpecificEntry(key = present("key2"), parentBookId = present(4L)) } returns null

        assertDoesNotThrow {
            scope.validateThat("key2").isUniqueEntryKey(4L)
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat("key1").isUniqueEntryKey(8L)
        }
        assertEquals(409, ex.code)
    }


    @Test
    fun `isUniqueSectionIndex works`() = runTest {
        coEvery {
            lawContentService.getSpecificSection(
                index = present("index1"),
                parentEntryId = present(8L)
            )
        } returns section
        coEvery {
            lawContentService.getSpecificSection(
                index = present("index2"),
                parentEntryId = present(4L)
            )
        } returns null

        assertDoesNotThrow {
            scope.validateThat("index2").isUniqueSectionIndex(4L)
        }

        val ex = assertThrows<RequestException> {
            scope.validateThat("index1").isUniqueSectionIndex(8L)
        }
        assertEquals(409, ex.code)
    }

}