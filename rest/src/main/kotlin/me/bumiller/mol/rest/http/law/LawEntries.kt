package me.bumiller.mol.rest.http.law

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.rest.response.law.entry.LawEntryResponse
import me.bumiller.mol.rest.util.user
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
    }
    route("law-books/law-entries/") {

    }
}

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