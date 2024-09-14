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
import me.bumiller.mol.rest.http.PathEntryId
import me.bumiller.mol.rest.http.PathSectionId
import me.bumiller.mol.rest.response.law.section.LawSectionResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.Validatable
import me.bumiller.mol.validation.actions.hasReadAccess
import me.bumiller.mol.validation.actions.hasWriteAccess
import me.bumiller.mol.validation.actions.isUniqueSectionIndex
import me.bumiller.mol.validation.validateThat
import me.bumiller.mol.validation.validateThatOptional
import me.bumiller.mol.validation.validated
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
    route("law-entries/{$PathEntryId}/law-sections/") {
        getByEntry(lawContentService)
        create(lawContentService)
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

@Serializable
private data class CreateLawSectionRequest(

    val index: String,

    val name: String,

    val content: String

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

private fun Route.getSpecific(lawContentService: LawContentService) = get("{$PathSectionId}/") {
    val sectionId = call.parameters.longOrBadRequest(PathSectionId)

    validateThat(user).hasReadAccess(lawSectionId = sectionId)

    val section = lawContentService.getSpecificSection(id = present(sectionId))!!
    val response = LawSectionResponse.create(section)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.update(lawContentService: LawContentService) = patch("{$PathSectionId}/") {
    val sectionId = call.parameters.longOrBadRequest(PathSectionId)
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

private fun Route.deleteSpecific(lawContentService: LawContentService) = delete("{$PathSectionId}/") {
    val sectionId = call.parameters.longOrBadRequest(PathSectionId)

    validateThat(user).hasWriteAccess(lawSectionId = sectionId)

    val deleted = lawContentService.deleteSection(sectionId)!!
    val response = LawSectionResponse.create(deleted)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.getByEntry(lawContentService: LawContentService) = get {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)

    validateThat(user).hasReadAccess(lawEntryId = entryId)
    val sections = lawContentService.getSectionsByEntry(entryId)!!
    val response = sections.map(LawSectionResponse.Companion::create)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.create(lawContentService: LawContentService) = post {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)
    val body = call.validated<CreateLawSectionRequest>()

    validateThat(user).hasReadAccess(lawEntryId = entryId)
    validateThat(body.index).isUniqueSectionIndex(entryId)

    val created = lawContentService.createSection(
        index = body.index,
        name = body.name,
        content = body.content,
        parentEntryId = entryId
    )!!
    val response = LawSectionResponse.create(created)

    call.respond(HttpStatusCode.OK, response)
}