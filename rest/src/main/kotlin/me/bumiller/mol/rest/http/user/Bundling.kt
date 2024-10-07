package me.bumiller.mol.rest.http.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.bumiller.mol.core.data.InvitationContentService
import me.bumiller.mol.core.data.LawContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.BookInvitation
import me.bumiller.mol.model.http.internal
import me.bumiller.mol.rest.response.law.book.LawBookResponse
import me.bumiller.mol.rest.response.law.entry.LawEntryResponse
import me.bumiller.mol.rest.response.law.invitation.BookInvitationResponse
import me.bumiller.mol.rest.response.law.section.LawSectionResponse
import me.bumiller.mol.rest.util.user
import me.bumiller.mol.validation.AccessValidator
import me.bumiller.mol.validation.ScopedPermission
import org.koin.ktor.ext.inject

/**
 * Adds the endpoints to /user/:
 *
 * GET /law-books/: Gets all books for the user
 * GET /law-entries/: Gets all entries for the user
 * GET /law-sections/: Gets all sections for the user
 */
internal fun Route.userBundled() {
    val lawContentService by inject<LawContentService>()
    val invitationContentService by inject<InvitationContentService>()
    val accessValidator by inject<AccessValidator>()

    route("user/") {
        route("law-books/") {
            allBooks(lawContentService)
        }
        route("law-entries/") {
            allEntries(lawContentService)
        }
        route("law-sections/") {
            allSections(lawContentService)
        }
        route("book-invitations/") {
            allInvitations(invitationContentService, lawContentService, accessValidator)
        }
    }
}

/**
 * Endpoint to GET /user/law-books/ that returns the law-books the user has access to
 */
private fun Route.allBooks(lawContentService: LawContentService) = get {
    try {
        val booksByMember = lawContentService.getBooksForMember(user.id)

        val responses = booksByMember
            .map(LawBookResponse.Companion::create)

        call.respond(HttpStatusCode.OK, responses)
    } catch (e: ServiceException.UserNotFound) {
        internal()
    }
}

/**
 * Endpoint to GET /user/law-entries/ that gets all the entries the user has access to
 */
private fun Route.allEntries(lawContentService: LawContentService) = get {
    val allBooks = try {
        lawContentService.getBooksForMember(user.id)
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
 * Endpoint to GET /user/law-sections/ that gets all law-sections the user has access to
 */
private fun Route.allSections(lawContentService: LawContentService) = get {
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
 * Endpoint to GET /user/book-invitations/ that gets all invitations the user has access to
 */
private fun Route.allInvitations(
    invitationContentService: InvitationContentService,
    lawContentService: LawContentService,
    accessValidator: AccessValidator
) = get {
    val byRecipient = invitationContentService.getAll(recipientId = user.id)
    val byAuthor = invitationContentService.getAll(authorId = user.id)

    val allBooks = lawContentService.getBooksForMember(user.id)
    val byBooks = allBooks.map { book ->
        val hasAccessToInvitations =
            accessValidator.resolveScoped(ScopedPermission.Books.Members.ReadInvitations(book.id), user.id, false)
        if (hasAccessToInvitations) invitationContentService.getAll(targetBookId = book.id)
        else emptyList()
    }.flatten()

    val allInvitations = (byRecipient + byAuthor + byBooks).distinctBy(BookInvitation::id)

    val response = allInvitations.map(BookInvitationResponse::create)
    call.respond(HttpStatusCode.OK, response)
}