package me.bumiller.mol.rest.law

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.coVerify
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.model.http.RequestException
import me.bumiller.mol.rest.http.law.CreateLawSectionRequest
import me.bumiller.mol.rest.response.law.section.LawSectionResponse
import me.bumiller.mol.test.*
import me.bumiller.mol.validation.ScopedPermission
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LawSectionsTest {

    private val user = userModel(1L).copy(isEmailVerified = true)

    @Test
    fun `GET law-sections_{id} checks user has read access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Sections.Read(1L),
                user.id
            )
        } throws RequestException(404, null)

        client.get("/test/api/law-sections/1/")

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Sections.Read(1L),
                user.id
            )
        }
    }

    @Test
    fun `GET law-sections_{id} returns the specific section`() = ktorEndpointTest(user) { services, client ->
        val section = lawSectionModel(1L)

        coEvery { services.lawContentService.getSpecificSection(present(1L), empty(), empty()) } returns section

        val res = client.get("/test/api/law-sections/1/")

        assertEquals(200, res.status.value)

        val body = res.body<LawSectionResponse>()
        assertEquals(section.id, body.id)
        assertEquals(section.content, body.content)
        assertEquals(section.index, body.index)

        coVerify(exactly = 1) { services.lawContentService.getSpecificSection(present(1L), empty(), empty()) }
    }

    @Test
    fun `PATCH law-sections_{id} checks user has write access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Sections.Write(1L),
                user.id
            )
        } throws RequestException(404, null)

        client.patch("/test/api/law-sections/1/") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {}
            """.trimIndent()
            )
        }

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Sections.Write(1L),
                user.id
            )
        }
    }

    @Test
    fun `PATCH law-sections_{id} calls updateSection with correct arguments and returns 200 with updated section`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L)
            val entry = lawEntryModel(1L)
            val section = lawSectionModel(1L)

            coEvery { services.lawContentService.getEntryForSection(section.id) } returns entry
            coEvery { services.lawContentService.getBookByEntry(entry.id) } returns book
            coEvery {
                services.lawContentService.getSpecificSection(
                    empty(),
                    present("index-243"),
                    present(entry.id)
                )
            } returns section //null
            coEvery { services.lawContentService.updateSection(any(), any(), any(), any()) } returns section

            val res = client.patch("/test/api/law-sections/1/") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                {
                  "index": "index-243",
                  "content": "content-532"
                }
            """.trimIndent()
                )
            }

            assertEquals(200, res.status.value)

            val body = res.body<LawSectionResponse>()
            assertEquals(section.id, body.id)
            assertEquals(section.content, body.content)
            assertEquals(section.index, body.index)

            coVerify {
                services.lawContentService.updateSection(
                    section.id,
                    present("index-243"),
                    empty(),
                    present("content-532")
                )
            }
        }

    @Test
    fun `DELETE law-sections_{id} checks user has write access`() = ktorEndpointTest(user) { services, client ->
        coEvery {
            services.accessValidator.resolveScoped(
                ScopedPermission.Sections.Write(1L),
                user.id
            )
        } throws RequestException(404, null)

        client.delete("/test/api/law-sections/1/")

        coVerify(exactly = 1) {
            services.accessValidator.resolveScoped(
                ScopedPermission.Sections.Write(1L),
                user.id
            )
        }
    }

    @Test
    fun `DELETE law-sections_{id} calls deleteSection with correct id and returns section`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L)
            val entry = lawEntryModel(1L)
            val section = lawSectionModel(1L)

            coEvery { services.lawContentService.getEntryForSection(section.id) } returns entry
            coEvery { services.lawContentService.getBookByEntry(entry.id) } returns book
            coEvery { services.lawContentService.deleteSection(entry.id) } returns section

            val res = client.delete("/test/api/law-sections/1/")

            assertEquals(200, res.status.value)

            val body = res.body<LawSectionResponse>()
            assertEquals(section.id, body.id)
            assertEquals(section.content, body.content)
            assertEquals(section.index, body.index)

            coVerify(exactly = 1) { services.lawContentService.deleteSection(1L) }
        }

    @Test
    fun `GET law-entries_{id}_law-sections checks user has read access`() =
        ktorEndpointTest(user) { services, client ->
            coEvery {
                services.accessValidator.resolveScoped(
                    ScopedPermission.Entries.Children.Read(1L),
                    user.id
                )
            } throws RequestException(404, null)

            client.get("/test/api/law-entries/1/law-sections/")

            coVerify(exactly = 1) {
                services.accessValidator.resolveScoped(
                    ScopedPermission.Entries.Children.Read(1L),
                    user.id
                )
            }
        }

    @Test
    fun `GET law-entries_{id}_law-sections returns only sections of entry`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L)
            val entry = lawEntryModel(1L)
            val section = lawSectionModel(1L)

            coEvery { services.lawContentService.getEntryForSection(section.id) } returns entry
            coEvery { services.lawContentService.getBookByEntry(entry.id) } returns book
            coEvery { services.lawService.isUserMemberOfEntry(user.id, entry.id) } returns true
            coEvery { services.lawContentService.getSectionsByEntry(entry.id) } answers { m ->
                when (m.invocation.args[0] as Long) {
                    1L -> lawSectionModels(3L, 10L)
                    else -> lawSectionModels(4L, 20L)
                }
            }

            val res = client.get("/test/api/law-entries/1/law-sections/")

            assertEquals(200, res.status.value)

            val body = res.body<List<LawSectionResponse>>()
            assertArrayEquals(arrayOf(10L, 11L, 12L), body.map(LawSectionResponse::id).sorted().toTypedArray())
        }

    @Test
    fun `POST law-entries_{id}_law-sections checks user has write access`() =
        ktorEndpointTest(user) { services, client ->
            coEvery {
                services.accessValidator.resolveScoped(
                    ScopedPermission.Entries.Children.Create(1L),
                    user.id
                )
            } throws RequestException(404, null)

            client.post("/test/api/law-entries/1/law-sections/") {
                contentType(ContentType.Application.Json)
                setBody(CreateLawSectionRequest("index-123", "name-535", "content-296"))
            }

            coVerify(exactly = 1) {
                services.accessValidator.resolveScoped(
                    ScopedPermission.Entries.Children.Create(1L),
                    user.id
                )
            }
        }

    @Test
    fun `POST law-entries_{id}_law-sections calls createSection with correct arguments and returns the section`() =
        ktorEndpointTest(user) { services, client ->
            val book = lawBookModel(1L)
            val entry = lawEntryModel(1L)
            val section = lawSectionModel(1L)

            coEvery { services.lawContentService.getEntryForSection(section.id) } returns entry
            coEvery { services.lawContentService.getBookByEntry(entry.id) } returns book
            coEvery { services.lawContentService.createSection(any(), any(), any(), any()) } returns section
            coEvery {
                services.lawContentService.getSpecificSection(
                    empty(),
                    present("index-123"),
                    present(1L)
                )
            } returns section

            val res = client.post("/test/api/law-entries/1/law-sections/") {
                contentType(ContentType.Application.Json)
                setBody(CreateLawSectionRequest("index-123", "name-535", "content-296"))
            }

            assertEquals(200, res.status.value)

            val body = res.body<LawSectionResponse>()
            assertEquals(section.id, body.id)
            assertEquals(section.content, body.content)
            assertEquals(section.index, body.index)

            coVerify { services.lawContentService.createSection("index-123", "name-535", "content-296", 1L) }
        }
}