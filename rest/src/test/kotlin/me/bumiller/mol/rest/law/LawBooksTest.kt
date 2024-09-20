package me.bumiller.mol.rest.law

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.runs
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.rest.http.law.CreateLawBookRequest
import me.bumiller.mol.rest.http.law.PutUserBookRoleRequest
import me.bumiller.mol.rest.response.law.book.LawBookResponse
import me.bumiller.mol.rest.response.user.BookRoleUserResponse
import me.bumiller.mol.rest.response.user.UserWithProfileResponse
import me.bumiller.mol.test.*
import me.bumiller.mol.validation.LawPermission
import me.bumiller.mol.validation.LawResourceScope
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LawBooksTest {

    private val profile = profileModel(1L)
    private val user = User(1L, "email@domain.com", "username", "password", true, profile)

    @Test
    fun `GET law-books returns 200 with books by member`() =
        ktorEndpointTest(user) { services, client ->
        coEvery { services.lawContentService.getBooksForMember(user.id) } returns lawBookModels(3, 4L)

            val res = client.get("/test/api/law-books/")
            val body = res.body<List<LawBookResponse>>()

            assertEquals(200, res.status.value)
        assertArrayEquals(
            (4L..6L).toList().sorted().toTypedArray(),
            body.map(LawBookResponse::id).sorted().toTypedArray()
        )
    }

    @Test
    fun `GET law-books returns 500 if user not found`() = ktorEndpointTest(user) { services, client ->
        coEvery { services.lawContentService.getBooksForMember(user.id) } throws ServiceException.UserNotFound(user.id)

        val res = client.get("/test/api/law-books/")

        assertEquals(500, res.status.value)
    }

    @Test
    fun `GET law-books_{id} checks read access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Read,
                1L,
                user.id
            )
        } throws RequestException(404, Unit)

        client.get("/test/api/law-books/1/")

        coVerify(exactly = 1) {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Read,
                1L,
                user.id
            )
        }
    }

    @Test
    fun `GET law-books_{id} returns 200 with book`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L, creator = user)
        coEvery { services.lawContentService.getSpecificBook(eq(book.id), any()) } returns book

        val res = client.get("/test/api/law-books/1/")
        val body = res.body<LawBookResponse>()

        assertEquals(200, res.status.value)
        assertEquals(book.id, body.id)
        assertEquals(book.key, body.key)
        assertEquals(book.creator.id, body.creatorId)
        assertEquals(book.description, body.description)
    }

    @Test
    fun `POST law-books returns 500 if user is not found`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.lawContentService.createBook(
                any(),
                any(),
                any(),
                any()
            )
        } throws ServiceException.UserNotFound(user.id)

        val res = client.post("/test/api/law-books/") {
            contentType(ContentType.Application.Json)
            setBody(CreateLawBookRequest("key", "name", "description"))
        }
        assertEquals(500, res.status.value)
    }

    @Test
    fun `POST law-books calls createBook with proper arguments and returns 200 with new book`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L)

            coEvery { services.lawContentService.createBook(any(), any(), any(), any()) } returns book

            val res = client.post("/test/api/law-books/") {
                contentType(ContentType.Application.Json)
                setBody(CreateLawBookRequest("key", "name", "description"))
            }
            val body = res.body<LawBookResponse>()

            coVerify(exactly = 1) { services.lawContentService.createBook("key", "name", "description", user.id) }

            assertEquals(201, res.status.value)
            assertEquals(book.name, body.name)
            assertEquals(book.key, body.key)
            assertEquals(book.description, body.description)
            assertEquals(user.id, body.creatorId)
        }

    @Test
    fun `PATCH law-books_{id} checks write access`() =
        ktorEndpointTest(user) { services, client ->
            coEvery {
                services.accessValidator.hasAccess(
                    LawResourceScope.Book,
                    LawPermission.Edit,
                    1L,
                    user.id
                )
            } throws RequestException(404, Unit)

            client.patch("/test/api/law-books/1/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {}
                """.trimIndent()
                )
            }

            coVerify(exactly = 1) {
                services.accessValidator.hasAccess(
                    LawResourceScope.Book,
                    LawPermission.Edit,
                    1L,
                    user.id
                )
            }
        }

    @Test
    fun `PATCH law-books_{id} calls updateBook with the correct arguments`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L)
            coEvery {
                services.lawContentService.updateBook(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns book

            val res = client.patch("/test/api/law-books/1/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "name": "new-name",
                      "key": "new-key"
                    }
                """.trimIndent()
                )
            }
            assertEquals(200, res.status.value)
            val body = res.body<LawBookResponse>()

            coVerify(exactly = 1) {
                services.lawContentService.updateBook(
                    1L,
                    present("new-key"),
                    present("new-name"),
                    empty(),
                    empty(),
                    empty()
                )
            }

            assertEquals(book.name, body.name)
            assertEquals(book.description, body.description)
            assertEquals(book.key, body.key)
        }

    @Test
    fun `DELETE law-books_{id} checks for write access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Edit,
                1L,
                user.id
            )
        } throws RequestException(404, Unit)

        client.delete("/test/api/law-books/1/")
        coVerify(exactly = 1) {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Edit,
                1L,
                user.id
            )
        }
    }

    @Test
    fun `DELETE law-books_{id} calls deleteBook with right argument`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L).copy(creator = user)
        coEvery { services.lawContentService.deleteBook(1L) } returns book

        val res1 = client.delete("/test/api/law-books/1/")
        assertEquals(200, res1.status.value)
        coVerify(exactly = 1) {
            services.lawContentService.deleteBook(
                1L
            )
        }
    }

    @Test
    fun `GET law-books_{id}_members checks user has read access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Read,
                1L,
                user.id
            )
        } throws RequestException(404, Unit)

        client.get("/test/api/law-books/1/members/")

        coVerify(exactly = 1) {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Read,
                1L,
                user.id
            )
        }
    }

    @Test
    fun `GET law-books_{id}_members returns members of book`() = ktorEndpointTest(user) { services, client ->
        coEvery { services.memberContentService.getMembersInBook(1L) } returns userModels(4L).map {
            it.copy(
                profile = profileModel(
                    1L
                )
            )
        }

        val res = client.get("/test/api/law-books/1/members/")

        assertEquals(200, res.status.value)
        val body = res.body<List<UserWithProfileResponse>>()

        assertArrayEquals(arrayOf(1L, 2L, 3L, 4L), body.map(UserWithProfileResponse::id).sorted().toTypedArray())
    }

    @Test
    fun `PUT law-books_{id}_members_{id} checks user has write access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Edit,
                1L,
                user.id
            )
        } throws RequestException(404, Unit)

        client.put("/test/api/law-books/1/members/1/")

        coVerify(exactly = 1) {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Edit,
                1L,
                user.id
            )
        }
    }

    @Test
    fun `PUT law-books_{id}_members_{id} returns 200 with current list of members if to be added user is already a member`() =
        ktorEndpointTest(user) { services, client ->
            coEvery {
                services.memberService.addMemberToBook(
                    any(), any()
                )
            } throws ServiceException.UserAlreadyMemberOfBook(1L, 1L)
            coEvery { services.memberContentService.getMembersInBook(1L) } returns userModels(4L).map { it.copy(profile = profile) }

            val res = client.put("/test/api/law-books/1/members/1/")

            assertEquals(200, res.status.value)

            val body = res.body<List<UserWithProfileResponse>>()
            assertEquals(4, body.size)
        }

    @Test
    fun `PUT law-books_{id}_members_{id} calls addMemberToBook with correct arguments and returns 200 with the result`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L).copy(creator = user)
            val toAddUser = userModel(7L).copy(isEmailVerified = true, profile = profile)

            coEvery {
                services.memberService.addMemberToBook(
                    book.id, toAddUser.id
                )
            } returns userModels(3L).map { it.copy(profile = profile) }

            val res = client.put("/test/api/law-books/1/members/7/")

            assertEquals(200, res.status.value)

            val body = res.body<List<UserWithProfileResponse>>()
            assertEquals(3, body.size)

            coVerify(exactly = 1) { services.memberService.addMemberToBook(1L, 7L) }
        }

    @Test
    fun `DELETE law-books_{id}_members_{id} checks user has write access`() =
        ktorEndpointTest(user) { services, client ->
            coEvery {
                services.accessValidator.hasAccess(
                    LawResourceScope.Book,
                    LawPermission.Edit,
                    1L,
                    user.id
                )
            } throws RequestException(404, Unit)

            client.delete("/test/api/law-books/1/members/1/")

            coVerify(exactly = 1) {
                services.accessValidator.hasAccess(
                    LawResourceScope.Book,
                    LawPermission.Edit,
                    1L,
                    user.id
                )
            }
        }

    @Test
    fun `DELETE law-books_{id}_members_{id} returns 200 with current list of members if to be deleted user was not found`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.memberService.removeMemberFromBook(1L, 1L) } throws ServiceException.UserNotFound(
                1L
            )
            coEvery { services.memberContentService.getMembersInBook(1L) } returns userModels(3).map { it.copy(profile = profile) }

            val res = client.delete("/test/api/law-books/1/members/1/")

            assertEquals(200, res.status.value)

            val body = res.body<List<UserWithProfileResponse>>()
            assertEquals(3, body.size)
        }

    @Test
    fun `DELETE law-books_{id}_members_{id} returns 200 with current list of members if to be deleted user was not member`() =
        ktorEndpointTest(user) { services, client ->
            coEvery {
                services.memberService.removeMemberFromBook(
                    1L,
                    1L
                )
            } throws ServiceException.UserNotMemberOfBook(
                1L,
                1L
            )
            coEvery { services.memberContentService.getMembersInBook(1L) } returns userModels(3).map { it.copy(profile = profile) }

            val res = client.delete("/test/api/law-books/1/members/1/")

            assertEquals(200, res.status.value)

            val body = res.body<List<UserWithProfileResponse>>()
            assertEquals(3, body.size)
        }

    @Test
    fun `DELETE law-books_{id}_members_{id} calls removeMemberFromBook with correct arguments and returns 200 with the result`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L).copy(creator = user)
            val toRemoveUser = userModel(7L).copy(isEmailVerified = true, profile = profile)

            coEvery {
                services.memberService.removeMemberFromBook(
                    book.id, toRemoveUser.id
                )
            } returns userModels(3L).map { it.copy(profile = profile) }

            val res = client.delete("/test/api/law-books/1/members/7/")

            assertEquals(200, res.status.value)

            val body = res.body<List<UserWithProfileResponse>>()
            assertEquals(3, body.size)

            coVerify(exactly = 1) { services.memberService.removeMemberFromBook(1L, 7L) }
        }

    @Test
    fun `GET law-books_{id}_roles checks for read access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Read,
                1L,
                user.id
            )
        } throws RequestException(404, Unit)

        client.get("/test/api/law-books/1/roles/")

        coVerify(exactly = 1) {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Read,
                1L,
                user.id
            )
        }
    }

    @Test
    fun `GET law-books_{id}_roles correctly combines members and roles and returns 200 with result`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.memberContentService.getMembersInBook(1L) } returns userModels(4L).map { it.copy(profile = profile) }
            coEvery { services.memberContentService.getMemberRole(any(), 1L) } answers { m ->
                when (m.invocation.args[0] as Long) {
                    1L -> MemberRole.Read
                    2L -> MemberRole.Write
                    3L -> MemberRole.Update
                    4L -> MemberRole.Admin
                    else -> error(Unit)
                }
            }

            val res = client.get("/test/api/law-books/1/roles/")

            assertEquals(200, res.status.value)

            val body = res.body<List<BookRoleUserResponse>>()

            body.forEach { memberWithRole ->
                val expectedRole = when (memberWithRole.user.id) {
                    1L -> MemberRole.Read.value
                    2L -> MemberRole.Write.value
                    3L -> MemberRole.Update.value
                    4L -> MemberRole.Admin.value
                    else -> error(Unit)
                }
                assertEquals(expectedRole, memberWithRole.role)
            }
        }

    @Test
    fun `GET law-books_{id}_roles_{id} checks for read access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Read,
                1L,
                user.id
            )
        } throws RequestException(404, Unit)

        client.get("/test/api/law-books/1/roles/1/")

        coVerify(exactly = 1) {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Read,
                1L,
                user.id
            )
        }
    }

    @Test
    fun `GET law-books_{id}_roles_{id} correctly combines member and role and returns 200 with result`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.userService.getSpecific(5L) } returns userModel(5L).copy(profile = profile)
            coEvery { services.memberContentService.getMemberRole(5L, 1L) } returns MemberRole.Admin

            val res = client.get("/test/api/law-books/1/roles/5/")

            assertEquals(200, res.status.value)

            val body = res.body<BookRoleUserResponse>()
            assertEquals(MemberRole.Admin.value, body.role)
            assertEquals(5L, body.user.id)
        }

    @Test
    fun `PUT law-books_{id}_roles_{id} checks for write access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Edit,
                1L,
                user.id
            )
        } throws RequestException(404, Unit)

        client.put("/test/api/law-books/1/roles/1/") {
            contentType(ContentType.Application.Json)
            setBody(PutUserBookRoleRequest(1))
        }

        coVerify(exactly = 1) {
            services.accessValidator.hasAccess(
                LawResourceScope.Book,
                LawPermission.Edit,
                1L,
                user.id
            )
        }
    }

    @Test
    fun `PUT law-books_{id}_roles_{id} calls setMemberRole with correct role and returns 200 with 204`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.memberService.setMemberRole(1L, 1L, any()) } just runs

            val res = client.put("/test/api/law-books/1/roles/1/") {
                contentType(ContentType.Application.Json)
                setBody(PutUserBookRoleRequest(1))
            }

            assertEquals(204, res.status.value)
        }

}