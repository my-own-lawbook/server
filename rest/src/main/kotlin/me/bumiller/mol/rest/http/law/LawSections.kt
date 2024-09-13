package me.bumiller.mol.rest.http.law

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.common.present
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.rest.response.law.section.LawSectionResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.rest.validation.hasReadAccess
import me.bumiller.mol.rest.validation.validateThat
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
    }
}

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