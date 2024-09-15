package me.bumiller.mol.rest.auth

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import me.bumiller.mol.model.*
import me.bumiller.mol.rest.http.auth.CreateUserRequest
import me.bumiller.mol.rest.http.auth.RequestEmailTokenRequest
import me.bumiller.mol.rest.http.auth.SubmitEmailTokenRequest
import me.bumiller.mol.rest.response.user.UserWithoutProfileResponse
import me.bumiller.mol.test.Services
import me.bumiller.mol.test.ktorEndpointTest
import me.bumiller.mol.test.testClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.time.Duration.Companion.minutes

class SignupTest {

    val profile = UserProfile(1L, LocalDate(2000, 1, 1), Gender.Other, "firstName", "lastName")
    val user = User(1L, "email@email.com", "username", "password", false, profile)
    val uuid: UUID = UUID.randomUUID()
    val token = TwoFactorToken(
        1L,
        uuid,
        null,
        Clock.System.now(),
        Clock.System.now().plus(5.minutes),
        TwoFactorTokenType.EmailConfirm,
        false,
        user
    )

    @Test
    fun `POST auth_signup calls createNewUser with arguments and responds the user`() = ktorEndpointTest {
        coEvery { Services.userService.getSpecific(any(), any(), any()) } returns null
        coEvery { Services.authService.createNewUser(any(), any(), any()) } returns user

        val res = testClient.post("/test/api/auth/signup/") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("username", "email@email.com", "password"))
        }

        coVerify(exactly = 1) { Services.authService.createNewUser("email@email.com", "username", "password") }
        assertEquals(201, res.status.value)
        assertEquals(
            UserWithoutProfileResponse(1L, "email@email.com", "username", false),
            res.body<UserWithoutProfileResponse>()
        )
    }

    @Test
    fun `POST auth_signup_email-verify only sends email when user is not yet verified and user has a profile set`() =
        ktorEndpointTest {
            coEvery { Services.userService.getSpecific(null, "email@email.com", null) } returnsMany listOf(
                user,
                user.copy(isEmailVerified = true),
                user.copy(profile = null)
            )
            coEvery { Services.authService.sendEmailVerification(any()) } returns token

            val res1 = testClient.post("/test/api/auth/signup/email-verify/") {
                contentType(ContentType.Application.Json)
                setBody(RequestEmailTokenRequest("email@email.com"))
            }
            assertEquals(202, res1.status.value)
            coVerify(exactly = 1) { Services.authService.sendEmailVerification(any()) }

            val res2 = testClient.post("/test/api/auth/signup/email-verify/") {
                contentType(ContentType.Application.Json)
                setBody(RequestEmailTokenRequest("email@email.com"))
            }
            assertEquals(202, res2.status.value)
            coVerify(exactly = 1) { Services.authService.sendEmailVerification(any()) }

            val res3 = testClient.post("/test/api/auth/signup/email-verify/") {
                contentType(ContentType.Application.Json)
                setBody(RequestEmailTokenRequest("email@email.com"))
            }
            assertEquals(202, res3.status.value)
            coVerify(exactly = 1) { Services.authService.sendEmailVerification(any()) }

        }

    @Test
    fun `PATCH auth_signup_email-verify calls validateEmailWithToken and returns 200 only if user is found`() =
        ktorEndpointTest {
            coEvery { Services.tokenService.getSpecific(any(), eq(uuid)) } returns token
            coEvery { Services.userService.getSpecific(any(), any(), any()) } returnsMany listOf(user, null)
            coEvery { Services.authService.validateEmailWithToken(uuid) } returns user

            val res1 = testClient.patch("/test/api/auth/signup/email-verify/") {
                contentType(ContentType.Application.Json)
                setBody(SubmitEmailTokenRequest(uuid.toStr()))
            }
            assertEquals(200, res1.status.value)
            coVerify(exactly = 1) { Services.authService.validateEmailWithToken(uuid) }

            val res2 = testClient.patch("/test/api/auth/signup/email-verify/") {
                contentType(ContentType.Application.Json)
                setBody(SubmitEmailTokenRequest(uuid.toStr()))
            }
            assertEquals(404, res2.status.value)
            coVerify(exactly = 1) { Services.authService.validateEmailWithToken(uuid) }
        }

}