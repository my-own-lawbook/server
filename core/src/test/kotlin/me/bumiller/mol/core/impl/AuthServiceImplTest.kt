package me.bumiller.mol.core.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.bumiller.mol.core.AuthService
import me.bumiller.mol.core.EncryptionService
import me.bumiller.mol.core.data.TwoFactorTokenService
import me.bumiller.mol.core.data.UserService
import me.bumiller.mol.email.EmailService
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class AuthServiceImplTest {

    lateinit var userService: UserService
    lateinit var tokenService: TwoFactorTokenService
    lateinit var encryptor: EncryptionService
    lateinit var emailService: EmailService

    lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        userService = mockk()
        tokenService = mockk()
        encryptor = mockk()
        emailService = mockk()

        authService = AuthServiceImpl(userService, tokenService, encryptor, emailService)

        coEvery { userService.createUser(any(), any(), any()) } returns user
        coEvery { encryptor.encrypt(any()) } returns ""
        coEvery { tokenService.create(any(), any(), any(), any(), any()) } returns token
        coEvery { emailService.sendEmailVerifyEmail(any(), any()) } returns Unit
    }

    @Test
    fun `createNewUser throws for duplicate email`() = runTest {
        coEvery { userService.getSpecific(any(),  any(), any()) } returns mockk()

        assertThrows<IllegalStateException> {
            authService.createNewUser("email", "", "", false)
        }
    }

    @Test
    fun `createNewUser throws for duplicate username`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns mockk()

        assertThrows<IllegalStateException> {
            authService.createNewUser("", "username", "", false)
        }
    }

    val user = User(1L, "", "", "", false, null)

    val uuid: UUID = UUID.randomUUID()
    val now = Clock.System.now()
    val token = TwoFactorToken(1L, uuid, "", now, now, TwoFactorTokenType.RefreshToken, false, user)

    @Test
    fun `createNewUser returns the user`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns null

        val returned = authService.createNewUser("", "", "", false)

        assertEquals(user, returned)
    }

    @Test
    fun `createNewUser only calls email service when set to`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns null

        authService.createNewUser("", "", "", false)
        authService.createNewUser("", "", "", true)
        authService.createNewUser("", "", "", true)

        coVerify(exactly = 2) {
            emailService.sendEmailVerifyEmail(any(), any())
        }
    }

    @Test
    fun `sendEmailVerification throws for null token`() = runTest {
        coEvery { tokenService.create(any(), any(), any(), any(), any()) } returns null

        assertThrows<IllegalStateException> {
            authService.sendEmailVerification(user)
        }
    }

    @Test
    fun `sendEmailVerification calls email service`() = runTest {
        authService.sendEmailVerification(user)

        coVerify { emailService.sendEmailVerifyEmail(user, any()) }
    }

    @Test
    fun `getAuthenticatedUser throws for invalid arg combination`() = runTest {
        assertThrows<IllegalArgumentException> {
            authService.getAuthenticatedUser("", "", "")
        }
        assertThrows<IllegalArgumentException> {
            authService.getAuthenticatedUser(null, null, "")
        }
    }

    @Test
    fun `getAuthenticatedUser returns null when no user found`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns null

        val returned = authService.getAuthenticatedUser(null, "", "")

        assertNull(returned)
    }

    @Test
    fun `getAuthenticatedUser returns null for invalid credentials and found user`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { encryptor.verify(any(), any()) } returns false

        val returned = authService.getAuthenticatedUser(null, "", "")

        assertNull(returned)
    }

    @Test
    fun `getAuthenticatedUser returns user when found and credentials valid`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { encryptor.verify(any(), any()) } returns true

        val returned = authService.getAuthenticatedUser(null, "", "")

        assertEquals(user, returned)
    }

    @Test
    fun `loginUser throws when user not found`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns null

        assertThrows<IllegalArgumentException> {
            authService.loginUser(1L)
        }
    }

    @Test
    fun `loginUser throws when token creation fails`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { tokenService.create(any(), any(), any(), any(), any()) } returns null

        assertThrows<IllegalStateException> {
            authService.loginUser(1L)
        }
    }

    @Test
    fun `loginUser jwt contains user as subject`() = runTest {
        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { tokenService.create(any(), any(), any(), any(), any()) } returns token

        val subjectOfJwt = authService.loginUser(1L).jwt.let { jwt ->
            JWT.require(Algorithm.HMAC256(AuthServiceImpl.JWT_SIGNING_SECRET))
                .build()
                .verify(jwt)
                .subject
        }

        assertEquals(user.email, subjectOfJwt)
    }

    @Test
    fun `logoutUser deletes only tokens for user`() = runTest {
        val uuids = (1..4).map { UUID.randomUUID() }
        val now = Clock.System.now()

        val user2 = User(2L, "email2", "username2", "password2", false, null)

        val tokens = listOf(
            TwoFactorToken(1L, uuids[0], null, now, now, TwoFactorTokenType.RefreshToken, false, user),
            TwoFactorToken(2L, uuids[1], null, now, now, TwoFactorTokenType.RefreshToken, false, user2),
            TwoFactorToken(3L, uuids[2], null, now, now, TwoFactorTokenType.RefreshToken, false, user),
            TwoFactorToken(4L, uuids[3], null, now, now, TwoFactorTokenType.RefreshToken, false, user2),
        )

        val tokenIdSlots = mutableListOf<Long>()

        coEvery { userService.getSpecific(any(), any(), any()) } returns user
        coEvery { tokenService.getSpecific(any(), any()) } answers { c -> tokens.find { it.token == (c.invocation.args[1] as UUID) }!! }
        coEvery { tokenService.markAsUsed(capture(tokenIdSlots)) } returns null

        authService.logoutUser(user.id, *tokens.map(TwoFactorToken::token).toTypedArray())

        val allowedTokenIds = tokens.filter { it.user.id == user.id }.map { it.id }
        tokenIdSlots.forEach { markedId ->
            assertTrue(markedId in allowedTokenIds)
        }
    }

}