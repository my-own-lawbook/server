package me.bumiller.mol.rest.http.law

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.rest.plugins.authenticatedUser
import me.bumiller.mol.rest.response.law.book.LawBookResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.validation.hasReadAccess
import me.bumiller.mol.rest.validation.validateThat
import org.koin.ktor.ext.inject

/**
 * Adds the endpoints to /:
 *
 *
 * - GET /law-books/: Get all law-books
 * - GET /law-books/:id/: Get a specific law-book
 * - POST /law-books/: Create a law-book
 * - PATCH /law-books/:id/: Perform a partial update on a law-book
 * - DELETE /law-books/:id/: Delete a law-book
 *
 */
internal fun Route.lawBooks() {
    val lawContentService by inject<LawContentService>()

    route("law-books/") {
        getAll(lawContentService)
        getById(lawContentService)
    }
}

//
// Endpoints
//

/**
 * Endpoint to GET /law-books/ that returns the law-books the user has access to
 */
private fun Route.getAll(lawContentService: LawContentService) = get {
    val user = call.authenticatedUser()

    val booksByCreator = lawContentService.getBooksByCreator(user.id)!!
    val booksByMember = lawContentService.getBooksByCreator(user.id)!!

    val responses = (booksByCreator + booksByMember)
        .map(LawBookResponse.Companion::create)

    call.respond(HttpStatusCode.OK, responses)
}

/**
 * Endpoint to GET /law-books/:id that returns a specific law-book
 */
private fun Route.getById(lawContentService: LawContentService) = get("{id}/") {
    val user = call.authenticatedUser()
    val bookId = call.parameters.longOrBadRequest("id")

    validateThat(user).hasReadAccess(lawBookId = bookId)

    val book = lawContentService.getSpecificBook(id = bookId)!!
    val response = LawBookResponse.create(book)
    call.respond(HttpStatusCode.OK, response)
}