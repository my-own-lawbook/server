package me.bumiller.mol.rest.users

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.mockk.coEvery
import me.bumiller.mol.rest.response.user.UserWithProfileResponse
import me.bumiller.mol.test.ktorEndpointTest
import me.bumiller.mol.test.profileModel
import me.bumiller.mol.test.userModel
import me.bumiller.mol.test.userModels
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UsersTest {

    private val user = userModel(1L).copy(isEmailVerified = true, profile = profileModel(1L))

    @Test
    fun `GET users returns 200 with all users`() = ktorEndpointTest(user) { services, client ->
        coEvery { services.userService.getAll() } returns userModels(4L).map { it.copy(profile = profileModel(1L)) }

        val res = client.get("/test/api/users/")

        assertEquals(200, res.status.value)

        val body = res.body<List<UserWithProfileResponse>>()
        assertEquals(4, body.size)
    }

}