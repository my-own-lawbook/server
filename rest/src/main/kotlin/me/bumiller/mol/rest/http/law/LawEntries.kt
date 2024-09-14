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
import me.bumiller.mol.rest.http.PathBookId
import me.bumiller.mol.rest.http.PathEntryId
import me.bumiller.mol.rest.response.law.entry.LawEntryResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.Validatable
import me.bumiller.mol.validation.actions.hasReadAccess
import me.bumiller.mol.validation.actions.hasWriteAccess
import me.bumiller.mol.validation.actions.isUniqueEntryKey
import me.bumiller.mol.validation.validateThat
import me.bumiller.mol.validation.validateThatOptional
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

    route("law-entries/") {
        getAll(lawContentService)
        getById(lawContentService)
        update(lawContentService)
        delete(lawContentService)
    }
    route("law-books/{$PathBookId}/law-entries/") {
        getById(lawContentService)
        create(lawContentService)
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

private fun Route.getAll(lawContentService: LawContentService) = get {
    val booksForUserCreated = lawContentService.getBooksByCreator(user.id)!!
    val booksForUserMember = lawContentService.getBooksForMember(user.id)!!
    val allBooks = booksForUserCreated + booksForUserMember

    val allEntries = allBooks.map { book ->
        lawContentService.getEntriesByBook(book.id)!!
    }.flatten()
    val response = allEntries.map(LawEntryResponse.Companion::create)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.getById(lawContentService: LawContentService) = get("{$PathEntryId}/") {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)

    validateThat(user).hasReadAccess(lawEntryId = entryId)

    val entry = lawContentService.getSpecificEntry(id = present(entryId))!!
    val response = LawEntryResponse.create(entry)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.update(lawContentService: LawContentService) = patch("{$PathEntryId}/") {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)
    val body = call.validated<UpdateLawEntryRequest>()

    validateThat(user).hasWriteAccess(lawEntryId = entryId)
    val book = lawContentService.getBookByEntry(entryId)!!
    validateThatOptional(body.key)?.isUniqueEntryKey(book.id)

    val updated = lawContentService.updateEntry(
        entryId = entryId,
        key = body.key,
        name = body.name
    )!!
    val response = LawEntryResponse.create(updated)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.delete(lawContentService: LawContentService) = delete("{$PathEntryId}/") {
    val entryId = call.parameters.longOrBadRequest(PathEntryId)

    validateThat(user).hasWriteAccess(lawEntryId = entryId)

    val deleted = lawContentService.deleteEntry(entryId)!!
    val response = LawEntryResponse.create(deleted)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.getByBook(lawContentService: LawContentService) = get {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    validateThat(user).hasReadAccess(lawEntryId = bookId)

    val entries = lawContentService.getEntriesByBook(bookId)!!
    val response = entries.map(LawEntryResponse.Companion::create)

    call.respond(HttpStatusCode.OK, response)
}

private fun Route.create(lawContentService: LawContentService) = post {
    val bookId = call.parameters.longOrBadRequest(PathBookId)
    val body = call.validated<CreateLawEntryRequest>()

    validateThat(body.key).isUniqueEntryKey(bookId)
    validateThat(user).hasWriteAccess(bookId)

    val created = lawContentService.createEntry(
        key = body.key,
        name = body.name,
        parentBookId = bookId
    )!!
    val response = LawEntryResponse.create(created)

    call.respond(HttpStatusCode.OK, response)
}