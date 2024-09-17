package me.bumiller.mol.rest.law

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.coVerify
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.model.User
import me.bumiller.mol.rest.response.law.book.LawBookResponse
import me.bumiller.mol.test.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LawBooksTest {

    private val profile = profileModel(1L)
    private val user = User(1L, "email@domain.com", "username", "password", true, profile)
    private val user2 = userModel(2L)

    @Test
    fun `GET law-books returns books by member and creator of user`() = ktorEndpointTest(user) { services, client ->
        coEvery { services.lawContentService.getBooksByCreator(user.id) } returns lawBookModels(3, 1L)
        coEvery { services.lawContentService.getBooksForMember(user.id) } returns lawBookModels(3, 4L)

        val body = client.get("/test/api/law-books/")
            .body<List<LawBookResponse>>()

        assertArrayEquals(
            (1L..6L).toList().sorted().toTypedArray(),
            body.map(LawBookResponse::id).sorted().toTypedArray()
        )
    }

    @Test
    fun `GET law-books_{id} returns 404 if user has no access to book`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L).copy(creator = user2)
        coEvery { services.lawContentService.getSpecificBook(eq(book.id), any(), any()) } returns book

        val res = client.get("/test/api/law-books/1/")
        assertEquals(404, res.status.value)
    }

    @Test
    fun `GET law-books_{id} returns book if user has access`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L, creator = user)
        coEvery { services.lawContentService.getSpecificBook(eq(book.id), any()) } returns book

        val body = client.get("/test/api/law-books/1/")
            .body<LawBookResponse>()
        assertEquals(book.id, body.id)
        assertEquals(book.key, body.key)
        assertEquals(book.creator.id, body.creatorId)
        assertEquals(book.description, body.description)
    }

    @Test
    fun `POST law-books calls createBook with proper arguments and return new book`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L)

            coEvery { services.lawContentService.getSpecificBook(any(), any(), any()) } returns null
            coEvery { services.lawContentService.createBook(any(), any(), any(), any()) } returns book

            val res = client.post("/test/api/law-books/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                {
                    "name": "name",
                    "key": "key",
                    "description": "description"
                }
            """.trimIndent()
                )
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
    fun `PATCH law-books_{id} returns 404 if user does not have access to the book`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L).copy(creator = user2)
            coEvery { services.lawContentService.getSpecificBook(eq(book.id), any(), any()) } returns book

            val res = client.patch("/test/api/law-books/1/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {}
                """.trimIndent()
                )
            }
            assertEquals(404, res.status.value)
        }

    @Test
    fun `PATCH law-books_{id} calls updateBook with the correct arguments`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L)
            coEvery { services.lawContentService.getSpecificBook(eq(1L), any(), any()) } returns book
            coEvery { services.lawContentService.getSpecificBook(any(), "new-key", any()) } returns null
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
        val book = lawBookModel(1L).copy(creator = user2)
        coEvery { services.lawContentService.getSpecificBook(any(), any(), any()) } returns book

        val res1 = client.delete("/test/api/law-books/1/")
        assertEquals(404, res1.status.value)
    }

    @Test
    fun `DELETE law-books_{id} calls deleteBook with right argument`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L).copy(creator = user)
        coEvery { services.lawContentService.getSpecificBook(any(), any(), any()) } returns book
        coEvery { services.lawContentService.deleteBook(1L) } returns book

        val res1 = client.delete("/test/api/law-books/1/")
        assertEquals(200, res1.status.value)
        coVerify(exactly = 1) {
            services.lawContentService.deleteBook(
                1L
            )
        }
    }

}