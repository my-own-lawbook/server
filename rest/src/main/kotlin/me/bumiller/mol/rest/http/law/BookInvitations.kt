package me.bumiller.mol.rest.http.law

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.core.data.InvitationContentService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.rest.http.PathInvitationId
import me.bumiller.mol.rest.response.law.invitation.BookInvitationResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.AccessValidator
import me.bumiller.mol.validation.ScopedPermission
import org.koin.ktor.ext.inject

/**
 * Adds the endpoints:
 *
 * Core:
 *
 * - GET /book-invitations/: Get invitations for all books the user is member of
 * - GET /book-invitations/:id/: Get specific invitation of a book the user has access to
 * - POST /book-invitations/:id/accept/: Accept an invitation of the invitation
 * - POST /book-invitations/:id/deny/: Deny an invitation of the invitation
 * - POST /book-invitations/:id/revoke/: Revoke an invitation the book the invitation was for
 *
 * For law-books:
 *
 * - GET /law-book/:id/book-invitations/: Gets invitations for a specific book that is member of the book
 * - POST /law-books/:id/book-invitations/: Creates a new invitation for the specified book that is an admin of the book
 *
 * For user:
 *
 * - GET /user/book-invitations/: Gets all invitations for the user
 */
internal fun Route.bookInvitations() {
    val lawContentService by inject<LawContentService>()
    val invitationContentService by inject<InvitationContentService>()
    val accessValidator by inject<AccessValidator>()

    route("book-invitations/") {
        getAll(lawContentService, invitationContentService, accessValidator)

        route("{$PathInvitationId}/") {
            getSpecific(invitationContentService, accessValidator)
        }
    }
}

//
// Endpoints
//

/**
 * Endpoint to GET /book-invitations/ that gets invitations for all books the user is member of
 */
private fun Route.getAll(
    lawContentService: LawContentService,
    invitationContentService: InvitationContentService,
    accessValidator: AccessValidator
) = get {
    val books = lawContentService.getBooksForMember(user.id)

    val invitations = books.map { book ->
        val canAccessInvitations =
            accessValidator.resolveScoped(ScopedPermission.Books.Members.ReadInvitations(book.id), user.id, false)
        if (canAccessInvitations) invitationContentService.getAll(targetBookId = book.id)
        else emptyList()
    }.flatten()

    val response = invitations.map(BookInvitationResponse::create)

    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to GET /book-invitations/ that gets invitations for all books the user is member of
 */
private fun Route.getSpecific(
    invitationContentService: InvitationContentService,
    accessValidator: AccessValidator
) = get {
    val invitationId = call.parameters.longOrBadRequest(PathInvitationId)
    val invitation = try {
        invitationContentService.getInvitationById(invitationId)
    } catch (e: ServiceException.InvitationNotFound) {
        null
    }

    accessValidator.resolveScoped(
        ScopedPermission.Books.Members.ReadInvitations(invitation?.targetBook?.id ?: -1),
        user.id
    )

    val response = BookInvitationResponse.create(invitation!!)

    call.respond(HttpStatusCode.OK, response)
}