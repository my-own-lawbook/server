package me.bumiller.mol.rest.http.law

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.rest.http.PathBookId
import me.bumiller.mol.rest.response.law.book.LawBookResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.*
import me.bumiller.mol.validation.actions.hasReadAccess
import me.bumiller.mol.validation.actions.hasWriteAccess
import me.bumiller.mol.validation.actions.isUniqueBookKey
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
        create(lawContentService)
        update(lawContentService)
        delete(lawContentService)
    }
}

//
// Request-Bodies
//

@Serializable
internal data class CreateLawBookRequest(

    val key: String,

    val name: String,

    val description: String

) : Validatable {

    override suspend fun ValidationScope.validate() {
        validateThat(key).isUniqueBookKey()
    }

}

@Serializable
internal data class UpdateLawBookRequest(

    val key: Optional<String> = empty(),

    val name: Optional<String> = empty(),

    val description: Optional<String> = empty()

) : Validatable {

    override suspend fun ValidationScope.validate() {
        validateThatOptional(key)?.isUniqueBookKey()
    }

}

//
// Endpoints
//

/**
 * Endpoint to GET /law-books/ that returns the law-books the user has access to
 */
private fun Route.getAll(lawContentService: LawContentService) = get {
    val booksByCreator = lawContentService.getBooksByCreator(user.id)!!
    val booksByMember = lawContentService.getBooksForMember(user.id)!!

    val responses = (booksByCreator + booksByMember)
        .map(LawBookResponse.Companion::create)

    call.respond(HttpStatusCode.OK, responses)
}

/**
 * Endpoint to GET /law-books/:id that returns a specific law-book
 */
private fun Route.getById(lawContentService: LawContentService) = get("{$PathBookId}/") {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    validateThat(user).hasReadAccess(lawBookId = bookId)

    val book = lawContentService.getSpecificBook(id = bookId)!!
    val response = LawBookResponse.create(book)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to POST /law-books/ that allows a user to add a new law-book
 */
private fun Route.create(lawContentService: LawContentService) = post {
    val body = call.validated<CreateLawBookRequest>()

    val created = lawContentService.createBook(body.key, body.name, body.description, user.id)!!
    val response = LawBookResponse.create(created)

    call.respond(HttpStatusCode.Created, response)
}

/**
 * Endpoint to PATCH /law-books/:id that allows a user to update an existing law-book
 */
private fun Route.update(lawContentService: LawContentService) = patch("{$PathBookId}/") {
    val body = call.validated<UpdateLawBookRequest>()
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    validateThat(user).hasWriteAccess(lawBookId = bookId)

    val updated = lawContentService.updateBook(
        bookId = bookId,
        key = body.key,
        name = body.name,
        description = body.description
    )!!
    val response = LawBookResponse.create(updated)

    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint for DELETE /law-books/:id/ that allows a user to delete a law-book
 */
private fun Route.delete(lawContentService: LawContentService) = delete("{$PathBookId}/") {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    validateThat(user).hasWriteAccess(lawBookId = bookId)

    val deleted = lawContentService.deleteBook(bookId)!!
    val response = LawBookResponse.create(deleted)

    call.respond(HttpStatusCode.OK, response)
}