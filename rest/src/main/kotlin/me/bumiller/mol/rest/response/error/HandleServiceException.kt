package me.bumiller.mol.rest.response.error

import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.http.conflict
import me.bumiller.mol.model.http.conflictUnique
import me.bumiller.mol.model.http.notFound
import me.bumiller.mol.model.http.notFoundIdentifier

const val NAME_TOKEN = "two-factor-token"

/**
 * Creates an appropriate http-error response for the given [ServiceException]
 */
internal fun ServiceException.handle(): Nothing = when (this) {
    is ServiceException.BookNoAdminLeft -> conflict("At least one admin per book is required.")
    is ServiceException.EmailTokenUserAlreadyVerified -> conflict("The user for the given token is already verified.")
    is ServiceException.InvalidTwoFactorTokenType -> notFoundIdentifier(NAME_TOKEN, token.toString())
    is ServiceException.LawBookKeyNotUnique -> conflictUnique("key", key)
    is ServiceException.LawBookNotFound -> notFoundIdentifier("law-book", id?.toString() ?: key ?: "")
    is ServiceException.LawEntryKeyNotUnique -> conflictUnique("key", key)
    is ServiceException.LawEntryNotFound -> notFoundIdentifier("law-entry", id?.toString() ?: key ?: "")
    is ServiceException.LawSectionIndexNotUnique -> conflictUnique("index", index)
    is ServiceException.LawSectionNotFound -> notFoundIdentifier("law-section", id?.toString() ?: index ?: key ?: "")
    is ServiceException.TwoFactorTokenExpired -> notFoundIdentifier(NAME_TOKEN, token.toString())
    is ServiceException.TwoFactorTokenNotFound -> notFoundIdentifier(NAME_TOKEN, token.toString())
    is ServiceException.TwoFactorTokenUsed -> notFoundIdentifier(NAME_TOKEN, token.toString())
    is ServiceException.UserAlreadyMemberOfBook -> conflict("User with id '$userId' is already member of book with id '$bookId'.")
    is ServiceException.UserEmailNotUnique -> conflictUnique("email", email)
    is ServiceException.UserNotFound -> notFoundIdentifier("user", id?.toString() ?: email ?: username ?: "")
    is ServiceException.UserNotFoundList -> notFound("Did not find a user for one of the id's in '${ids.joinToString()}'")
    is ServiceException.UserNotMemberOfBook -> conflict("User with id '$userId' is not a member of book '$bookId'.")
    is ServiceException.UserProfileAlreadyPresent -> conflict("User with id '$userId' already has a profile set.")
    is ServiceException.UserProfileNotPresent -> conflict("User with id '$userId' does not yet have a profile set.")
    is ServiceException.UserUsernameNotUnique -> conflictUnique("username", username)
    is ServiceException.InvitationExpired -> conflict("Invitation with id '$id' is expired.")
    is ServiceException.InvitationNotFound -> notFoundIdentifier("book-invitation", id.toString())
    is ServiceException.InvitationNotOpen -> conflict("Invitation with id '$id' is not open.")
    is ServiceException.OpenInvitationAlreadyPresent -> conflict("User with id 'userId' already has an open invitation to book with id '$bookId'.")
}