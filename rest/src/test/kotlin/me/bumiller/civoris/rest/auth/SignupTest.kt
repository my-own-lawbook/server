package me.bumiller.civoris.rest.auth

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import me.bumiller.civoris.model.*
import me.bumiller.civoris.rest.http.auth.CreateUserRequest
import me.bumiller.civoris.rest.http.auth.SubmitEmailTokenRequest
import me.bumiller.civoris.rest.response.user.AuthUserWithoutProfileResponse
import me.bumiller.civoris.test.ktorEndpointTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.time.Duration.Companion.minutes

class SignupTest {

    val profile = UserProfile(1L, LocalDate(2000, 1, 1), Gender.Other, "firstName", "lastName")
    val user = User(1L, "email@email.com", "username", "password", false, profile)
    val tokenString = UUID.randomUUID().toString()
    val token = TwoFactorToken(
        1L,
        tokenString,
        null,
        Clock.System.now(),
        Clock.System.now().plus(5.minutes),
        TwoFactorTokenType.EmailConfirm,
        false,
        user
    )

    @Test
    fun `POST auth_signup calls createNewUser with arguments and responds the user`() =
        ktorEndpointTest { services, client ->
        coEvery { services.authService.createNewUser(any(), any(), any()) } returns user

            val res = client.post("/test/api/auth/signup/") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("username", "email@email.com", "password"))
        }

        coVerify(exactly = 1) { services.authService.createNewUser("email@email.com", "username", "password") }
        assertEquals(201, res.status.value)
        assertEquals(
            AuthUserWithoutProfileResponse(1L, "email@email.com", "username", false),
            res.body<AuthUserWithoutProfileResponse>()
        )
    }

    @Test
    fun `POST auth_signup_email-verify returns 409 if user already has the email verified`() =
        ktorEndpointTest(user.copy(isEmailVerified = true, profile = profile)) { services, client ->
            val res1 = client.post("/test/api/auth/signup/email-verify/")

            assertEquals(409, res1.status.value)
        }

    @Test
    fun `PATCH auth_signup_email-verify calls validateEmailWithToken and returns 200`() =
        ktorEndpointTest { services, client ->
            coEvery { services.authService.validateEmailWithToken(tokenString) } returns user

            val res1 = client.patch("/test/api/auth/signup/email-verify/") {
                contentType(ContentType.Application.Json)
                setBody(SubmitEmailTokenRequest(tokenString.toStr()))
            }
            assertEquals(200, res1.status.value)
            coVerify(exactly = 1) { services.authService.validateEmailWithToken(tokenString) }
        }

}