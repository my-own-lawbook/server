package me.bumiller.mol.rest.user

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.mockk.coEvery
import me.bumiller.mol.rest.response.law.book.LawBookResponse
import me.bumiller.mol.rest.response.law.entry.LawEntryResponse
import me.bumiller.mol.test.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BundledTest {

    private val profile = profileModel(1L)
    private val user = userModel(1L).copy(isEmailVerified = true, profile = profile)

    @Test
    fun `GET law-books returns 200 with books by member`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.lawContentService.getBooksForMember(user.id) } returns lawBookModels(3, 4L)

            val res = client.get("/test/api/user/law-books/")
            assertEquals(200, res.status.value)

            val body = res.body<List<LawBookResponse>>()

            assertArrayEquals(
                (4L..6L).toList().sorted().toTypedArray(),
                body.map(LawBookResponse::id).sorted().toTypedArray()
            )
        }

    @Test
    fun `GET law-entries returns 200 with all law entries the user had access to`() =
        ktorEndpointTest(user) { services, client ->
            coEvery { services.lawContentService.getBooksForMember(1L) } returns lawBookModels(2, 3)

            coEvery { services.lawContentService.getEntriesByBook(any()) } answers { m ->
                when ((m.invocation.args[0] as Long)) {
                    3L -> lawEntryModels(2, 5)
                    4L -> lawEntryModels(2, 7)
                    else -> throw Error()
                }
            }

            val res = client.get("/test/api/user/law-entries/")

            assertEquals(200, res.status.value)

            val body = res.body<List<LawEntryResponse>>()
            assertArrayEquals((5L..8L).toList().toTypedArray(), body.map(LawEntryResponse::id).sorted().toTypedArray())
        }

}