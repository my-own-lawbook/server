package me.bumiller.mol.rest.http.law

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.empty
import me.bumiller.mol.core.InvitationService
import me.bumiller.mol.core.data.InvitationContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.BookInvitation
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.http.unauthorized
import me.bumiller.mol.rest.http.PathBookId
import me.bumiller.mol.rest.http.PathInvitationId
import me.bumiller.mol.rest.response.law.invitation.BookInvitationResponse
import me.bumiller.mol.rest.util.longOrBadRequest
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.*
import me.bumiller.mol.validation.actions.isInFuture
import me.bumiller.mol.validation.actions.isMemberRole
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
    val invitationContentService by inject<InvitationContentService>()
    val invitationService by inject<InvitationService>()
    val accessValidator by inject<AccessValidator>()

    route("book-invitations/") {
        route("{$PathInvitationId}/") {
            getSpecific(invitationContentService, accessValidator)

            route("accept/") {
                accept(invitationContentService, invitationService, accessValidator)
            }
            route("deny/") {
                deny(invitationContentService, invitationService, accessValidator)
            }
            route("revoke/") {
                revoke(invitationContentService, invitationService, accessValidator)
            }
        }
    }

    route("law-books/{$PathBookId}/book-invitations/") {
        allForBook(invitationContentService, accessValidator)
        createInvitation(invitationService, accessValidator)
    }
}

//
// Utility functions
//
private suspend fun AccessValidator.validateReadAccessToInvitation(
    userId: Long,
    invitationId: Long,
    invitationContentService: InvitationContentService
): BookInvitation {
    val invitation = try {
        invitationContentService.getInvitationById(invitationId)
    } catch (e: ServiceException.InvitationNotFound) {
        null
    }

    resolveScoped(
        ScopedPermission.Books.Members.ReadInvitations(invitation?.targetBook?.id ?: -1),
        userId
    )

    return invitation!!
}

private suspend fun AccessValidator.validateManageAccessToInvitation(
    userId: Long,
    invitationId: Long,
    invitationContentService: InvitationContentService
): BookInvitation {
    val invitation = try {
        invitationContentService.getInvitationById(invitationId)
    } catch (e: ServiceException.InvitationNotFound) {
        null
    }

    resolveScoped(
        ScopedPermission.Books.Members.ManageInvitations(invitation?.targetBook?.id ?: -1),
        userId
    )

    return invitation!!
}

//
// Request bodies
//
@Serializable
private data class CreateInvitationRequest(

    @SerialName("target_book_id")
    val targetBookId: Long,

    @SerialName("recipient_id")
    val recipientId: Long,

    val role: Optional<Int> = empty(),

    @SerialName("expires_at")
    val expiresAt: Optional<Instant?> = empty(),

    val message: Optional<String?> = empty()

) : Validatable {

    override suspend fun validate() {
        validateThatOptional(role)?.isMemberRole()
        expiresAt.ifPresent { instant ->
            instant?.let {
                validateThat(instant).isInFuture()
            }
        }
    }

}

//
// Endpoints
//

/**
 * Endpoint to GET /book-invitations/ that gets invitations for all books the user is member of
 */
private fun Route.getSpecific(
    invitationContentService: InvitationContentService,
    accessValidator: AccessValidator
) = get {
    val invitationId = call.parameters.longOrBadRequest(PathInvitationId)
    val invitation = accessValidator.validateReadAccessToInvitation(user.id, invitationId, invitationContentService)

    val response = BookInvitationResponse.create(invitation)

    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to POST /book-invitations/:id/accept/ that accepts an invitation
 */
private fun Route.accept(
    invitationContentService: InvitationContentService,
    invitationService: InvitationService,
    accessValidator: AccessValidator
) = post {
    val invitationId = call.parameters.longOrBadRequest(PathInvitationId)
    val invitation = accessValidator.validateReadAccessToInvitation(user.id, invitationId, invitationContentService)

    if (invitation.recipient.id != user.id)
        unauthorized()
    invitationService.acceptInvitation(invitation.id)

    call.respond(HttpStatusCode.NoContent)
}

/**
 * Endpoint to POST /book-invitations/:id/deny/ that denies an invitation
 */
private fun Route.deny(
    invitationContentService: InvitationContentService,
    invitationService: InvitationService,
    accessValidator: AccessValidator
) = post {
    val invitationId = call.parameters.longOrBadRequest(PathInvitationId)
    val invitation = accessValidator.validateReadAccessToInvitation(user.id, invitationId, invitationContentService)

    if (invitation.recipient.id != user.id)
        unauthorized()
    invitationService.denyInvitation(invitation.id)

    call.respond(HttpStatusCode.NoContent)
}

/**
 * Endpoint to POST /book-invitations/:id/revoke/ that revokes an invitation
 */
private fun Route.revoke(
    invitationContentService: InvitationContentService,
    invitationService: InvitationService,
    accessValidator: AccessValidator
) = post {
    val invitationId = call.parameters.longOrBadRequest(PathInvitationId)
    val invitation = accessValidator.validateManageAccessToInvitation(user.id, invitationId, invitationContentService)

    invitationService.revokeInvitation(invitation.id)

    call.respond(HttpStatusCode.NoContent)
}

/**
 * Endpoint to /law-books/:id/book-invitations/ that gets all invitations for a specific book
 */
private fun Route.allForBook(
    invitationContentService: InvitationContentService,
    accessValidator: AccessValidator
) = get {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    accessValidator.resolveScoped(ScopedPermission.Books.Members.ReadInvitations(bookId), user.id)

    val invitations = invitationContentService.getAll(targetBookId = bookId)

    val response = invitations.map(BookInvitationResponse::create)
    call.respond(HttpStatusCode.OK, response)
}

/**
 * Endpoint to POST /law-books/:id/book-invitations/ that creates a new invitation for a specific book
 */
private fun Route.createInvitation(
    invitationService: InvitationService,
    accessValidator: AccessValidator
) = post {
    val bookId = call.parameters.longOrBadRequest(PathBookId)

    accessValidator.resolveScoped(ScopedPermission.Books.Members.ManageInvitations(bookId), user.id)

    val body = call.validated<CreateInvitationRequest>()
    val role = if (body.role.isPresent)
        MemberRole.entries.first { it.value == body.role.get() }
    else MemberRole.Member

    val invitation = invitationService.createInvitation(
        authorId = user.id,
        targetBookId = bookId,
        recipientId = body.recipientId,
        role = role,
        expiresAt = body.expiresAt.getOrNull(),
        message = body.message.getOrNull()
    )

    val response = BookInvitationResponse.create(invitation)
    call.respond(HttpStatusCode.Created, response)
}