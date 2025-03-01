package me.bumiller.civoris.rest.users

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.mockk.coEvery
import io.mockk.coVerify
import me.bumiller.civoris.model.http.RequestException
import me.bumiller.civoris.rest.response.user.UserWithProfileResponse
import me.bumiller.civoris.test.ktorEndpointTest
import me.bumiller.civoris.test.profileModel
import me.bumiller.civoris.test.userModel
import me.bumiller.civoris.test.userModels
import me.bumiller.civoris.validation.GlobalPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UsersTest {

    private val user = userModel(1L).copy(isEmailVerified = true, profile = profileModel(1L))

    @Test
    fun `GET users checks for read access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveGlobal(
                GlobalPermission.Users.Read(profile = true, email = false),
                user.id
            )
        } throws RequestException(404, null)

        val res = client.get("/test/api/users/")

        assertEquals(404, res.status.value)

        coVerify { services.accessValidator.resolveGlobal(GlobalPermission.Users.Read(true, email = false), user.id) }
    }

    @Test
    fun `GET users returns 200 with all users`() = ktorEndpointTest(user) { services, client ->
        coEvery { services.userService.getAll() } returns userModels(4L).map { it.copy(profile = profileModel(1L)) }

        val res = client.get("/test/api/users/")

        assertEquals(200, res.status.value)

        val body = res.body<List<UserWithProfileResponse>>()
        assertEquals(4, body.size)
    }

}