package me.bumiller.mol.rest.law

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.runs
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.rest.response.law.invitation.BookInvitationResponse
import me.bumiller.mol.test.*
import me.bumiller.mol.validation.ScopedPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BookInvitationsTest {

    private val user = userModel(1L).copy(isEmailVerified = true, profile = profileModel(1L))

    @Test
    fun `GET book-invitations_{id} checks for permission`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Invitations.Read(1L),
                user.id
            )
        } throws RequestException(404, "")

        client.get("/test/api/book-invitations/1/")

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Invitations.Read(1L),
                user.id
            )
        }
    }

    @Test
    fun `GET book-invitations_{id} returns invitation`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.invitationContentService.getInvitationById(1L)
        } returns invitationModel(1L)

        val res = client.get("/test/api/book-invitations/1/")

        assertEquals(200, res.status.value)

        val body = res.body<BookInvitationResponse>()
        assertEquals(1L, body.id)
    }

    @Test
    fun `POST book-invitations_{id}_accept checks for permission`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Invitations.Accept(1L),
                user.id
            )
        } throws RequestException(404, "")

        client.post("/test/api/book-invitations/1/accept/")

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Invitations.Accept(1L),
                user.id
            )
        }
    }

    @Test
    fun `POST book-invitations_{id}_accept calls acceptInvitation with correct id and returns 204`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.invitationService.acceptInvitation(any()) } just runs

            val res = client.post("/test/api/book-invitations/6/accept/")

            assertEquals(204, res.status.value)
            coVerify(exactly = 1) {
                services.invitationService.acceptInvitation(6L)
            }
        }

    @Test
    fun `POST book-invitations_{id}_deny checks for permission`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Invitations.Deny(1L),
                user.id
            )
        } throws RequestException(404, "")

        client.post("/test/api/book-invitations/1/deny/")

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Invitations.Deny(1L),
                user.id
            )
        }
    }

    @Test
    fun `POST book-invitations_{id}_deny calls denyInvitation with correct id and returns 204`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.invitationService.denyInvitation(any()) } just runs

            val res = client.post("/test/api/book-invitations/6/deny/")

            assertEquals(204, res.status.value)
            coVerify(exactly = 1) {
                services.invitationService.denyInvitation(6L)
            }
        }

    @Test
    fun `POST book-invitations_{id}_revoke checks for permission`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Invitations.Revoke(1L),
                user.id
            )
        } throws RequestException(404, "")

        client.post("/test/api/book-invitations/1/revoke/")

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Invitations.Revoke(1L),
                user.id
            )
        }
    }

    @Test
    fun `POST book-invitations_{id}_revoke calls revokeInvitation with correct id and returns 204`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.invitationService.revokeInvitation(any()) } just runs

            val res = client.post("/test/api/book-invitations/6/revoke/")

            assertEquals(204, res.status.value)
            coVerify(exactly = 1) {
                services.invitationService.revokeInvitation(6L)
            }
        }

    @Test
    fun `GET law-books_{id}_book-invitations checks for permission`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Books.Members.ReadInvitations(5L),
                user.id
            )
        } throws RequestException(404, "")

        client.get("/test/api/law-books/5/book-invitations/")

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Books.Members.ReadInvitations(5L),
                user.id
            )
        }
    }

    @Test
    fun `GET law-books_{id}_book-invitations calls getAll with correct id and returns 200 with invitations`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.invitationContentService.getAll(targetBookId = 7L) } returns invitationModels(4L)

            val res = client.get("/test/api/law-books/7/book-invitations/")

            assertEquals(200, res.status.value)

            val body = res.body<List<BookInvitationResponse>>()
            assertEquals(4, body.size)
        }

    @Test
    fun `POST law-books_{id}_book-invitations checks for permission`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Books.Members.ManageInvitations(5L),
                user.id
            )
        } throws RequestException(404, "")

        client.post("/test/api/law-books/5/book-invitations/")

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Books.Members.ManageInvitations(5L),
                user.id
            )
        }
    }

    @Test
    fun `POST law-books_{id}_book-invitations calls createInvitation with correct arguments and returns 201 with created invitation`() =
        ktorEndpointTest(user) { services, client ->
            coEvery {
                services.invitationService.createInvitation(any(), any(), any(), any(), any(), any())
            } returns invitationModel(123L)

            val res = client.post("/test/api/law-books/5/book-invitations/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "recipient_id": 19,
                      "role": 1,
                      "message": null
                    }
                """.trimIndent()
                )
            }

            coVerify(exactly = 1) {
                services.invitationService.createInvitation(
                    authorId = user.id,
                    targetBookId = 5L,
                    recipientId = 19L,
                    role = MemberRole.Member,
                    expiresAt = null,
                    message = null
                )
            }

            assertEquals(201, res.status.value)

            val body = res.body<BookInvitationResponse>()
            assertEquals(123L, body.id)
        }

}