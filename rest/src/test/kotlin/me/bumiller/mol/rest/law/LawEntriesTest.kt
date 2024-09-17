package me.bumiller.mol.rest.law

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.coVerify
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.rest.response.law.entry.LawEntryResponse
import me.bumiller.mol.test.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LawEntriesTest {

    private val user = userModel(1L).copy(isEmailVerified = true)
    private val user2 = userModel(2L)

    @Test
    fun `GET law-entries returns all law entries the user had access to`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.lawContentService.getBooksByCreator(1L) } returns lawBookModels(2)
            coEvery { services.lawContentService.getBooksForMember(1L) } returns lawBookModels(2, 3)
            coEvery { services.lawContentService.getEntriesByBook(any()) } answers { m ->
                when ((m.invocation.args[0] as Long)) {
                    1L -> lawEntryModels(2, 1)
                    2L -> lawEntryModels(2, 3)
                    3L -> lawEntryModels(2, 5)
                    4L -> lawEntryModels(2, 7)
                    else -> throw Error()
                }
            }

            val res = client.get("/test/api/law-entries/")

            assertEquals(200, res.status.value)

            val body = res.body<List<LawEntryResponse>>()
            assertArrayEquals((1L..8L).toList().toTypedArray(), body.map(LawEntryResponse::id).sorted().toTypedArray())
        }

    @Test
    fun `GET law-entries_{id} checks that user has read access`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L, creator = user2)

        coEvery { services.lawContentService.getBookByEntry(1L) } returns book
        coEvery { services.lawService.isUserMemberOfEntry(1L, 1L) } returns false

        val res = client.get("/test/api/law-entries/1/")

        assertEquals(404, res.status.value)
    }

    @Test
    fun `GET law-entries_{id} returns specific entry`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L, creator = user)
        val entry = lawEntryModel(1L)

        coEvery { services.lawContentService.getBookByEntry(1L) } returns book
        coEvery { services.lawContentService.getSpecificEntry(present(1L), any(), any()) } returns entry

        val res = client.get("/test/api/law-entries/1/")

        assertEquals(200, res.status.value)

        val body = res.body<LawEntryResponse>()
        assertEquals(entry.id, body.id)
        assertEquals(entry.name, body.name)
        assertEquals(entry.key, body.key)
    }

    @Test
    fun `UPDATE law-entries_{id} checks user has write access`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L, creator = user2)

        coEvery { services.lawContentService.getBookByEntry(1L) } returns book

        val res = client.patch("/test/api/law-entries/1/") {
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
    fun `UPDATE law-entries_{id} checks that key is unique`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L, creator = user)
        val entry = lawEntryModel(1L)

        coEvery { services.lawContentService.getBookByEntry(1L) } returns book
        coEvery {
            services.lawContentService.getSpecificEntry(
                empty(), present("key-123"), present(book.id)
            )
        } returns entry

        val res = client.patch("/test/api/law-entries/1/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "key": "key-123"
                }
            """.trimIndent()
            )
        }

        assertEquals(409, res.status.value)
    }

    @Test
    fun `UPDATE law-entries_{id} calls updateEntry with correct arguments`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L, creator = user)
            val entry = lawEntryModel(1L)

            coEvery { services.lawContentService.getBookByEntry(1L) } returns book
            coEvery {
                services.lawContentService.getSpecificEntry(
                    empty(), present("key-123"), present(book.id)
                )
            } returns null
            coEvery { services.lawContentService.updateEntry(any(), any(), any()) } returns entry

            val res = client.patch("/test/api/law-entries/1/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                {
                  "key": "key-123",
                  "name": "name-653"
                }
            """.trimIndent()
                )
            }

            assertEquals(200, res.status.value)

            val body = res.body<LawEntryResponse>()
            assertEquals(entry.id, body.id)
            assertEquals(entry.name, body.name)
            assertEquals(entry.key, body.key)

            coVerify(exactly = 1) {
                services.lawContentService.updateEntry(
                    1L, present("key-123"), present("name-653")
                )
            }
        }

    @Test
    fun `DELETE law-entries_{id} checks that user has write access`() = ktorEndpointTest(user) { services, client ->
        val book = lawBookModel(1L, creator = user2)

        coEvery { services.lawContentService.getBookByEntry(1L) } returns book

        val res = client.delete("/test/api/law-entries/1/")

        assertEquals(404, res.status.value)
    }

    @Test
    fun `DELETE law-entries_{id} calls deleteEntry with correct id and returns deleted entry`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L, creator = user)
            val entry = lawEntryModel(1L)

            coEvery { services.lawContentService.getBookByEntry(1L) } returns book
            coEvery { services.lawContentService.deleteEntry(1L) } returns entry

            val res = client.delete("/test/api/law-entries/1/")

            assertEquals(200, res.status.value)
            val body = res.body<LawEntryResponse>()

            assertEquals(entry.id, body.id)
            assertEquals(entry.name, body.name)
            assertEquals(entry.key, body.key)

            coVerify(exactly = 1) { services.lawContentService.deleteEntry(1L) }
        }

    @Test
    fun `GET law-books_{id}_law-entries checks that user has write access`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L, creator = user2)

            coEvery { services.lawContentService.getBookByEntry(1L) } returns book
            coEvery { services.lawService.isUserMemberOfEntry(user.id, 1L) } returns false

            val res = client.get("/test/api/law-books/1/law-entries/")

            assertEquals(404, res.status.value)
        }

    @Test
    fun `GET law-books_{id}_law-entries returns only entries of given book`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L, creator = user)

            coEvery { services.lawContentService.getBookByEntry(1L) } returns book
            coEvery { services.lawService.isUserMemberOfEntry(user.id, 1L) } returns true
            coEvery { services.lawContentService.getEntriesByBook(any()) } answers { m ->
                when (m.invocation.args[0] as Long) {
                    1L -> lawEntryModels(3, 10L)
                    else -> lawEntryModels(5, 20L)
                }
            }

            val res = client.get("/test/api/law-books/1/law-entries/")

            assertEquals(200, res.status.value)

            val body = res.body<List<LawEntryResponse>>()
            assertArrayEquals(
                (10L..12L).toList().toTypedArray(),
                body.map(LawEntryResponse::id).sorted().toTypedArray()
            )
        }

    @Test
    fun `CREATE law-books_{id}_law-entries checks user has write access`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L, creator = user2)

            coEvery { services.lawContentService.getSpecificBook(1L) } returns book

            val res = client.post("/test/api/law-books/1/law-entries/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "key": "key-253",
                      "name": "name-532"
                    }
                """.trimIndent()
                )
            }

            assertEquals(404, res.status.value)
        }

    @Test
    fun `CREATE law-books_{id}_law-entries checks key is unique`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L, creator = user)
            val entry = lawEntryModel(1L)

            coEvery { services.lawContentService.getSpecificBook(1L) } returns book
            coEvery {
                services.lawContentService.getSpecificEntry(
                    empty(),
                    present("key-253"),
                    present(1L)
                )
            } returns entry

            val res = client.post("/test/api/law-books/1/law-entries/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "key": "key-253",
                      "name": "name-532"
                    }
                """.trimIndent()
                )
            }

            assertEquals(409, res.status.value)
        }

    @Test
    fun `CREATE law-books_{id}_law-entries calls createEntry with correct argumentd`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L, creator = user)
            val entry = lawEntryModel(1L)

            coEvery { services.lawContentService.getSpecificBook(1L) } returns book
            coEvery { services.lawContentService.createEntry(any(), any(), any()) } returns entry
            coEvery {
                services.lawContentService.getSpecificEntry(
                    empty(),
                    present("key-253"),
                    present(1L)
                )
            } returns null

            val res = client.post("/test/api/law-books/1/law-entries/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "key": "key-253",
                      "name": "name-532"
                    }
                """.trimIndent()
                )
            }

            assertEquals(200, res.status.value)

            val body = res.body<LawEntryResponse>()
            assertEquals(entry.id, body.id)
            assertEquals(entry.name, body.name)
            assertEquals(entry.key, body.key)

            coVerify { services.lawContentService.createEntry("key-253", "name-532", 1L) }
        }

}