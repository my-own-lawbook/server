package me.bumiller.civoris.rest.auth

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.Clock
import me.bumiller.civoris.model.AuthTokens
import me.bumiller.civoris.model.TwoFactorToken
import me.bumiller.civoris.model.TwoFactorTokenType
import me.bumiller.civoris.model.User
import me.bumiller.civoris.rest.http.auth.LoginRefreshRequest
import me.bumiller.civoris.rest.response.user.TokenResponse
import me.bumiller.civoris.test.ktorEndpointTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class LoginTest {

    private val user = User(1L, "email@domain.com", "username", "password", true, null)

    private val tokenString = UUID.randomUUID().toString()
    private val token =
        TwoFactorToken(1L, tokenString, null, Clock.System.now(), null, TwoFactorTokenType.RefreshToken, false, user)

    @Test
    fun `POST auth_login returns tokens when passed valid credentials`() = ktorEndpointTest { services, client ->
        coEvery { services.authService.getAuthenticatedUser("email@domain.com", null, "password") } returns user
        coEvery { services.authService.getAuthenticatedUser(null, "username", "password") } returns user
        coEvery { services.authService.loginUser(1L) } returns AuthTokens("jwt", token)

        val res1 = client.post("/test/api/auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "email": "email@domain.com",
                  "password": "password"
                }
            """.trimIndent()
            )
        }
        assertEquals(200, res1.status.value)
        assertEquals(tokenString.toString(), res1.body<TokenResponse>().refreshToken)

        val res2 = client.post("/test/api/auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "username": "username",
                  "password": "password"
                }
            """.trimIndent()
            )
        }
        assertEquals(200, res2.status.value)
        assertEquals(tokenString.toString(), res2.body<TokenResponse>().refreshToken)
    }

    @Test
    fun `POST auth_login returns 401 for invalid credentials`() = ktorEndpointTest { services, client ->
        coEvery { services.authService.getAuthenticatedUser("email@domain.com", null, "password") } returns null
        coEvery { services.authService.getAuthenticatedUser(null, "username", "password") } returns null

        val res1 = client.post("/test/api/auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "email": "email@domain.com",
                  "password": "password"
                }
            """.trimIndent()
            )
        }
        assertEquals(401, res1.status.value)

        val res2 = client.post("/test/api/auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "username": "username",
                  "password": "password"
                }
            """.trimIndent()
            )
        }
        assertEquals(401, res2.status.value)
    }

    @Test
    fun `POST auth_login doesn't allow email and username at same time`() = ktorEndpointTest { _, client ->
        val res1 = client.post("/test/api/auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "email": "email@domain.com",
                  "password": "password",
                  "username": "username"
                }
            """.trimIndent()
            )
        }
        assertEquals(400, res1.status.value)
    }

    @Test
    fun `POST auth_login_refresh returns tokens`() = ktorEndpointTest { services, client ->
        coEvery { services.authService.loginUserWithRefreshToken(tokenString) } returns AuthTokens("jwt", token)

        val res = client.post("/test/api/auth/login/refresh/") {
            contentType(ContentType.Application.Json)
            setBody(LoginRefreshRequest(tokenString.toString()))
        }

        assertEquals(200, res.status.value)
        assertEquals(tokenString.toString(), res.body<TokenResponse>().refreshToken)
    }

}