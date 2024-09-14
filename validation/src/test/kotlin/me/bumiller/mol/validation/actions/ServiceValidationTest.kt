package me.bumiller.mol.validation.actions

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.LawService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.validation.ValidationScope
import me.bumiller.mol.validation.validateThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.time.Duration.Companion.days

class ServiceValidationTest {

    val tokenService: TwoFactorTokenService = mockk()

    val scope = object : ValidationScope {
        override val tokenService = this@ServiceValidationTest.tokenService
        override val userService: UserService = mockk()
        override val authService: AuthService = mockk()
        override val lawService: LawService = mockk()
        override val lawContentService: LawContentService = mockk()
    }

    @Test
    fun `isTokenValid throws if did not find`() = runTest {
        coEvery { tokenService.getSpecific(any(), any()) } returns null

        val ex = assertThrows<RequestException> {
            scope.validateThat(UUID.randomUUID()).isTokenValid()
        }
        assertEquals(404, ex.code)
    }

    val user = User(1L, "email", "username", "password", true, null)
    val token = TwoFactorToken(
        1L,
        UUID.randomUUID(),
        null,
        Clock.System.now().minus(5.days),
        Clock.System.now().plus(5.days),
        TwoFactorTokenType.RefreshToken,
        false,
        user
    )

    @Test
    fun `isTokenValid throws if type doesnt match`() = runTest {
        coEvery { tokenService.getSpecific(any(), any()) } returns token

        val ex = assertThrows<RequestException> {
            scope.validateThat(UUID.randomUUID()).isTokenValid(TwoFactorTokenType.EmailConfirm)
        }
        assertEquals(404, ex.code)

        assertDoesNotThrow {
            scope.validateThat(UUID.randomUUID()).isTokenValid(TwoFactorTokenType.RefreshToken)
        }
    }

    @Test
    fun `isTokenValid throws if user doesnt match`() = runTest {
        coEvery { tokenService.getSpecific(any(), any()) } returns token

        val ex = assertThrows<RequestException> {
            scope.validateThat(UUID.randomUUID()).isTokenValid(userId = 5L)
        }
        assertEquals(404, ex.code)

        assertDoesNotThrow {
            scope.validateThat(UUID.randomUUID()).isTokenValid(userId = 1L)
        }
        assertDoesNotThrow {
            scope.validateThat(UUID.randomUUID()).isTokenValid(userId = null)
        }
    }

    @Test
    fun `isTokenValid throws if token is expired`() = runTest {
        coEvery { tokenService.getSpecific(any(), any()) } returnsMany listOf(
            token.copy(
                expiringAt = Clock.System.now().minus(1.days)
            ), token
        )

        val ex = assertThrows<RequestException> {
            scope.validateThat(UUID.randomUUID()).isTokenValid()
        }
        assertEquals(404, ex.code)

        assertDoesNotThrow {
            scope.validateThat(UUID.randomUUID()).isTokenValid()
        }
    }

    @Test
    fun `isTokenValid throws if token is used`() = runTest {
        coEvery { tokenService.getSpecific(any(), any()) } returnsMany listOf(token.copy(used = true), token)

        val ex = assertThrows<RequestException> {
            scope.validateThat(UUID.randomUUID()).isTokenValid()
        }
        assertEquals(404, ex.code)

        assertDoesNotThrow {
            scope.validateThat(UUID.randomUUID()).isTokenValid()
        }
    }

}