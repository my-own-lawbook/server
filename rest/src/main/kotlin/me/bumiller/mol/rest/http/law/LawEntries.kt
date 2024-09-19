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
import me.bumiller.mol.rest.http.PathBookId
import me.bumiller.mol.rest.http.PathEntryId
import me.bumiller.mol.rest.response.law.entry.LawEntryResponse
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
 * - GET /law-entries/ -> Get all entries
 * - GET /law-entries/:id/ -> Get specific entry
 * - PATCH /law-entries/:id/ -> Perform partial update
 * - DELETE /law-entries/:id/ -> Delete
 *
 *
 * - GET /law-books/:id/law-entries/ -> Get all by parent book
 * - POST /law-books/:id/law-entries/ -> Create new
 */
internal fun Route.lawEntries() {
    val lawContentService by inject<LawContentService>()
    val accessValidator by inject<AccessValidator>()

    route("law-entries/") {
        getAll(lawContentService)
        getById(lawContentService, accessValidator)
        update(lawContentService, accessValidator)
        delete(lawContentService, accessValidator)
    }
    route("law-books/{$PathBookId}/law-entries/") {
        getByBook(lawContentService, accessValidator)
        create(lawContentService, accessValidator)
    }
}

//
// Request-Bodies
//

@Serializable
internal data class UpdateLawEntryRequest(

    val key: Optional<String> = empty(),

    val name: Optional<String> = empty()

) : Validatable

@Serializable
internal data class CreateLawEntryRequest(

    val key: String,

    val name: String

) : Validatable

//
// Endpoints
//

/**
 * Endpoint to GET /law-entries/ that gets all the entries the user has access to
 */
private fun Route.getAll(lawContentService: LawContentService) = get {
    val allBooks = try {
        val booksForUserCreated = lawContentService.getBooksByCreator(user.id)
        val booksForUserMember = lawContentService.getBooksForMember(user.id)
        booksForUserCreated + booksForUserMember
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }

    val allEntries = allBooks.map { book ->
        try {
            lawContentService.getEntriesByBook(book.id)
        } catch (e: ServiceException.LawBookNotFound) {
            internal()
        }
    }.flatten()

    val response = allEntries.map(LawEntryResponse.Companion::create)

    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to GET /law-entries/:id/ that gets a specific law entry
 */
private fun Route.getById(lawContentService: LawContentService, accessValidator: AccessValidator) =
    get("{$PathEntryId}/") {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)

        accessValidator.validateReadEntry(user, entryId)

        val entry = lawContentService.getSpecificEntry(id = present(entryId))

    val response = LawEntryResponse.create(entry)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to PATCH /law-entries/:id/ that perform a partial update on a law-entry
 */
private fun Route.update(lawContentService: LawContentService, accessValidator: AccessValidator) =
    patch("{$PathEntryId}/") {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)

    val body = call.validated<UpdateLawEntryRequest>()

        accessValidator.validateWriteEntry(user, entryId)

    val updated = lawContentService.updateEntry(
        entryId = entryId,
        key = body.key,
        name = body.name
    )

    val response = LawEntryResponse.create(updated)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to DELETE /law-entries/:id/ that deletes a law-entry
 */
private fun Route.delete(lawContentService: LawContentService, accessValidator: AccessValidator) =
    delete("{$PathEntryId}/") {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)

        accessValidator.validateWriteEntry(user, entryId)

        val deleted = lawContentService.deleteEntry(entryId)

    val response = LawEntryResponse.create(deleted)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to GET /law-books/:id/law-entries/ that get law-entries by a book
 */
private fun Route.getByBook(lawContentService: LawContentService, accessValidator: AccessValidator) = get {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    accessValidator.validateReadBook(user, bookId)

    val entries = lawContentService.getEntriesByBook(bookId)

    val response = entries.map(LawEntryResponse.Companion::create)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to POST /law-books/:id/law-entries/ that creates a new law-entry
 */
private fun Route.create(lawContentService: LawContentService, accessValidator: AccessValidator) = post {
    val bookId = call.parameters.longOrBadRequest(PathBookId)
    val body = call.validated<CreateLawEntryRequest>()

    accessValidator.validateWriteBook(user, bookId)

    val created = lawContentService.createEntry(
        key = body.key,
        name = body.name,
        parentBookId = bookId
    )

    val response = LawEntryResponse.create(created)
    call.respond(HttpStatusCode.OK, response)
}