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
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.rest.http.PathEntryId
import me.bumiller.mol.rest.http.PathSectionId
import me.bumiller.mol.rest.response.law.section.LawSectionResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.AccessValidator
import me.bumiller.mol.validation.Validatable
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
    val accessValidator by inject<AccessValidator>()

    route("law-sections/") {
        getAll(lawContentService)
        getSpecific(lawContentService, accessValidator)
        update(lawContentService, accessValidator)
        deleteSpecific(lawContentService, accessValidator)
    }
    route("law-entries/{$PathEntryId}/law-sections/") {
        getByEntry(lawContentService, accessValidator)
        create(lawContentService, accessValidator)
    }
}

//
// Request bodies
//

@Serializable
internal data class UpdateLawSectionRequest(

    val index: Optional<String> = empty(),

    val name: Optional<String> = empty(),

    val content: Optional<String> = empty()

) : Validatable

@Serializable
internal data class CreateLawSectionRequest(

    val index: String,

    val name: String,

    val content: String

) : Validatable

//
// Endpoints
//

/**
 * Endpoint to GET /law-sections/ that gets all law-sections the user has access to
 */
private fun Route.getAll(lawContentService: LawContentService) = get {
    val booksForUser = try {
        lawContentService.getBooksForMember(user.id)
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    val entries = booksForUser.map { book ->
        try {
            lawContentService.getEntriesByBook(book.id)
        } catch (e: ServiceException.LawBookNotFound) {
            internal()
        }
    }.flatten()

    val sections = entries.map { entry ->
        try {
            lawContentService.getSectionsByEntry(entry.id)
        } catch (e: ServiceException.LawEntryNotFound) {
            internal()
        }
    }.flatten()

    val response = sections.map(LawSectionResponse.Companion::create)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to GET /law-sections/:id/ that gets a specific section
 */
private fun Route.getSpecific(lawContentService: LawContentService, accessValidator: AccessValidator) =
    get("{$PathSectionId}/") {
    val sectionId = call.parameters.longOrBadRequest(PathSectionId)

        accessValidator.validateReadSection(user, sectionId)

        val section = lawContentService.getSpecificSection(id = present(sectionId))

    val response = LawSectionResponse.create(section)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to PATCH /law-sections/:id/ that performs a partial update on a law-section
 */
private fun Route.update(lawContentService: LawContentService, accessValidator: AccessValidator) =
    patch("{$PathSectionId}/") {
    val sectionId = call.parameters.longOrBadRequest(PathSectionId)

    val body = call.validated<UpdateLawSectionRequest>()

        accessValidator.validateWriteSection(user, sectionId)

    val updated = lawContentService.updateSection(
        sectionId = sectionId,
        index = body.index,
        name = body.name,
        content = body.content
    )

    val response = LawSectionResponse.create(updated)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to DELETE /law-sections/:id/ that deletes a law-section
 */
private fun Route.deleteSpecific(lawContentService: LawContentService, accessValidator: AccessValidator) =
    delete("{$PathSectionId}/") {
    val sectionId = call.parameters.longOrBadRequest(PathSectionId)

        accessValidator.validateWriteSection(user, sectionId)

        val deleted = lawContentService.deleteSection(sectionId)

    val response = LawSectionResponse.create(deleted)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to GET /law-entries/:id/law-sections/ that gets all sections in a specific entry
 */
private fun Route.getByEntry(lawContentService: LawContentService, accessValidator: AccessValidator) = get {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)

    accessValidator.validateReadEntry(user, entryId)

    val sections = lawContentService.getSectionsByEntry(entryId)

    val response = sections.map(LawSectionResponse.Companion::create)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to POST /law-entries/:id/law-sections/ that creates a new law-section
 */
private fun Route.create(lawContentService: LawContentService, accessValidator: AccessValidator) = post {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)

    val body = call.validated<CreateLawSectionRequest>()

    accessValidator.validateWriteEntry(user, entryId)

    val created = lawContentService.createSection(
        index = body.index,
        name = body.name,
        content = body.content,
        parentEntryId = entryId
    )

    val response = LawSectionResponse.create(created)
    call.respond(HttpStatusCode.OK, response)
}