package me.bumiller.mol.rest.user

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import kotlinx.datetime.LocalDate
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.Gender
import me.bumiller.mol.model.User
import me.bumiller.mol.model.UserProfile
import me.bumiller.mol.rest.http.user.CreateProfileRequest
import me.bumiller.mol.rest.response.user.AuthUserWithProfileResponse
import me.bumiller.mol.rest.response.user.UserProfileResponse
import me.bumiller.mol.test.ktorEndpointTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProfileTest {

    private val profile = UserProfile(1L, LocalDate(2000, 1, 1), Gender.Other, "John", "Doe")
    private val user = User(1L, "email@domain.com", "username", "password", true, profile)


    @Test
    fun `POST user_profile returns 500 if user is not found`() = ktorEndpointTest(user) { services, client ->
        coEvery { services.userService.createProfile(any(), any()) } throws ServiceException.UserNotFound(1L)

        val res = client.post("/test/api/user/profile/") {
            contentType(ContentType.Application.Json)
            setBody(CreateProfileRequest("firstName", "lastName", Gender.Other, LocalDate(2000, 1, 1)))
        }

        assertEquals(500, res.status.value)
    }


    @Test
    fun `POST user_profile calls createProfile with correct arguments and responds updated user`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.userService.getSpecific(any(), eq(user.email), any()) } returns user.copy(profile = null)
            val profileSlot = slot<UserProfile>()
            coEvery { services.userService.createProfile(user.id, capture(profileSlot)) } returns user

            val res = client.post("/test/api/user/profile/") {
                contentType(ContentType.Application.Json)
                setBody(CreateProfileRequest("firstName", "lastName", Gender.Female, LocalDate(2000, 1, 2)))
            }
            assertEquals(200, res.status.value)
            assertEquals(user.id, res.body<AuthUserWithProfileResponse>().id)

            assertEquals(LocalDate(2000, 1, 2), profileSlot.captured.birthday)
            assertEquals(Gender.Female, profileSlot.captured.gender)
            assertEquals("firstName", profileSlot.captured.firstName)
            assertEquals("lastName", profileSlot.captured.lastName)
            assertEquals(profile.id, profileSlot.captured.id)
        }


    @Test
    fun `GET user_profile returns the profile of the user`() = ktorEndpointTest(user) { _, client ->
        val res = client.get("/test/api/user/profile/")
        val body = res.body<UserProfileResponse>()

        assertEquals(profile.birthday, body.birthday)
        assertEquals(profile.firstName, body.firstName)
        assertEquals(profile.lastName, body.lastName)
        assertEquals(profile.gender, body.gender)
    }

    @Test
    fun `PATCH user_profile correctly calls updateProfile`() = ktorEndpointTest(user) { services, client ->
        coEvery { services.userService.updateProfile(any(), any(), any(), any(), any()) } returns user

        client.patch("/test/api/user/profile/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "first_name": "Peter",
                  "last_name": "Griffin"
                }
            """.trimIndent()
            )
        }
        coVerify(exactly = 1) {
            services.userService.updateProfile(
                user.id,
                present("Peter"),
                present("Griffin"),
                empty(),
                empty()
            )
        }

        client.patch("/test/api/user/profile/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "gender": "male"
                }
            """.trimIndent()
            )
        }


        coVerify(exactly = 1) {
            services.userService.updateProfile(
                user.id,
                empty(),
                empty(),
                empty(),
                present(Gender.Male)
            )
        }
    }

    @Test
    fun `PATCH user_profile returns the user`() = ktorEndpointTest(user) { services, client ->
        coEvery { services.userService.updateProfile(any(), any(), any(), any(), any()) } returns user

        val body = client.patch("/test/api/user/profile/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "first_name": "Peter",
                  "last_name": "Griffin"
                }
            """.trimIndent()
            )
        }.body<AuthUserWithProfileResponse>()


        assertEquals(user.id, body.id)
        assertEquals(user.email, body.email)
        assertEquals(profile.firstName, body.profile.firstName)
        assertEquals(profile.birthday, body.profile.birthday)
    }

}