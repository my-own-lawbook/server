package me.bumiller.mol.rest.auth

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.Clock
import me.bumiller.mol.model.AuthTokens
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import me.bumiller.mol.model.User
import me.bumiller.mol.rest.http.auth.LoginRefreshRequest
import me.bumiller.mol.rest.response.user.TokenResponse
import me.bumiller.mol.test.ktorEndpointTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class LoginTest {

    private val user = User(1L, "email@domain.com", "username", "password", true, null)

    private val uuid: UUID = UUID.randomUUID()
    private val token =
        TwoFactorToken(1L, uuid, null, Clock.System.now(), null, TwoFactorTokenType.RefreshToken, false, user)

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
        assertEquals(uuid.toString(), res1.body<TokenResponse>().refreshToken)

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
        assertEquals(uuid.toString(), res2.body<TokenResponse>().refreshToken)
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
        coEvery { services.authService.loginUserWithRefreshToken(uuid) } returns AuthTokens("jwt", token)

        val res = client.post("/test/api/auth/login/refresh/") {
            contentType(ContentType.Application.Json)
            setBody(LoginRefreshRequest(uuid.toString()))
        }

        assertEquals(200, res.status.value)
        assertEquals(uuid.toString(), res.body<TokenResponse>().refreshToken)
    }

}