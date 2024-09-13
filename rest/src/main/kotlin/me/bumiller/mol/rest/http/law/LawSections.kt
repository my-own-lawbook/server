package me.bumiller.mol.rest.http.law

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.common.present
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.rest.response.law.section.LawSectionResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.rest.validation.*
import org.koin.ktor.ext.inject

/**
 * Adds the endpoints to /:
 *
 *
 * - GET /law-sections/ -> Get all
 * - GET /law-sections/:id/ -> Get specific
 * - PATCH /law-sections/:id/ -> Partial update
 * - DELETE /law-sections/:id/ -> Delete
 *
 * - GET /law-entries/:id/law-sections -> Get all for entry
 * - POST /law-entries/:id/ -> Create new inside entry
 */
internal fun Route.lawSections() {
    val lawContentService by inject<LawContentService>()

    route("law-sections/") {
        getAll(lawContentService)
        getSpecific(lawContentService)
        update(lawContentService)
        deleteSpecific(lawContentService)
    }
    route("law-entries/{entryId}/law-sections/") {
        getByEntry(lawContentService)
    }
}

//
// Request bodies
//

@Serializable
private data class UpdateLawSectionRequest(

    val index: Optional<String> = empty(),

    val name: Optional<String> = empty(),

    val content: Optional<String> = empty()

) : Validatable

//
// Endpoints
//

private fun Route.getAll(lawContentService: LawContentService) = get {
    val booksForUserCreated = lawContentService.getBooksByCreator(user.id)!!
    val booksForUserMember = lawContentService.getBooksForMember(user.id)!!
    val booksForUser = booksForUserCreated + booksForUserMember

    val entries = booksForUser.map { book ->
        lawContentService.getEntriesByBook(book.id)!!
    }.flatten()

    val sections = entries.map { entry ->
        lawContentService.getSectionsByEntry(entry.id)!!
    }.flatten()
    val response = sections.map(LawSectionResponse.Companion::create)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.getSpecific(lawContentService: LawContentService) = get("{sectionId}/") {
    val sectionId = call.parameters.longOrBadRequest("sectionId")

    validateThat(user).hasReadAccess(lawSectionId = sectionId)

    val section = lawContentService.getSpecificSection(id = present(sectionId))!!
    val response = LawSectionResponse.create(section)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.update(lawContentService: LawContentService) = patch("{sectionId}/") {
    val sectionId = call.parameters.longOrBadRequest("sectionId")
    val body = call.validated<UpdateLawSectionRequest>()

    validateThat(user).hasWriteAccess(lawSectionId = sectionId)
    val entry = lawContentService.getEntryForSection(sectionId)!!
    validateThatOptional(body.index)?.isUniqueSectionIndex(entry.id)

    val updated = lawContentService.updateSection(
        sectionId = sectionId,
        index = body.index,
        name = body.name,
        content = body.content
    )!!
    val response = LawSectionResponse.create(updated)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.deleteSpecific(lawContentService: LawContentService) = delete("{sectionId}/") {
    val sectionId = call.parameters.longOrBadRequest("sectionId")

    validateThat(user).hasWriteAccess(lawSectionId = sectionId)

    val deleted = lawContentService.deleteSection(sectionId)!!
    val response = LawSectionResponse.create(deleted)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.getByEntry(lawContentService: LawContentService) = get {
    val entryId = call.parameters.longOrBadRequest("entryId")

    validateThat(user).hasReadAccess(lawEntryId = entryId)
    val sections = lawContentService.getSectionsByEntry(entryId)!!
    val response = sections.map(LawSectionResponse.Companion::create)

    call.respond(HttpStatusCode.OK, response)
}