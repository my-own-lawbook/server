package me.bumiller.mol.core.exception

import kotlinx.datetime.Instant
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.TwoFactorTokenType
import java.util.*

/**
 * An exception thrown by services when any kind of error occurs
 */
@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
sealed class ServiceException : RuntimeException() {

    /**
     * A user was not found
     */
    data class UserNotFound(val id: Long? = null, val email: String? = null, val username: String? = null) :
        ServiceException()

    /**
     * A user of a list of user ids was not found
     */
    data class UserNotFoundList(val ids: List<Long>) : ServiceException()

    /**
     * A law-book was not found
     */
    data class LawBookNotFound(val id: Long? = null, val key: String? = null) : ServiceException()

    /**
     * A law-book key already existed
     */
    data class LawBookKeyNotUnique(val key: String) : ServiceException()

    /**
     * A law-entry was not found
     */
    data class LawEntryNotFound(val id: Long? = null, val key: String? = null) : ServiceException()

    /**
     * A law-entry key was not unique
     */
    data class LawEntryKeyNotUnique(val key: String) : ServiceException()

    /**
     * A law-section was not found
     */
    data class LawSectionNotFound(val id: Long? = null, val index: String? = null, val key: String? = null) :
        ServiceException()

    /**
     * A law-section index was not unique
     */
    data class LawSectionIndexNotUnique(val index: String) : ServiceException()

    /**
     * A user that is already part of a book was tried to be added again
     */
    data class UserAlreadyMemberOfBook(val userId: Long, val bookId: Long) : ServiceException()

    /**
     * A user is not a member of a book
     */
    data class UserNotMemberOfBook(val userId: Long, val bookId: Long) : ServiceException()

    /**
     * A book would not have an admin left after an operation
     */
    data class BookNoAdminLeft(val bookId: Long) : ServiceException()

    /**
     * A two-factor-token could not be found
     */
    data class TwoFactorTokenNotFound(val id: Long? = null, val token: UUID? = null) : ServiceException()

    /**
     * A user already has a profile set
     */
    data class UserProfileAlreadyPresent(val userId: Long) : ServiceException()

    /**
     * A user has no profile set
     */
    data class UserProfileNotPresent(val userId: Long) : ServiceException()

    /**
     * An email is already taken
     */
    data class UserEmailNotUnique(val email: String) : ServiceException()

    /**
     * A username is already taken
     */
    data class UserUsernameNotUnique(val username: String) : ServiceException()

    /**
     * A token had an unexpected type
     */
    data class InvalidTwoFactorTokenType(val token: UUID, val expectedType: TwoFactorTokenType) : ServiceException()

    /**
     * A token that is already expired
     */
    data class TwoFactorTokenExpired(val token: UUID, val expiredSince: Instant?) : ServiceException()

    /**
     * A token that is already used
     */
    data class TwoFactorTokenUsed(val token: UUID) : ServiceException()

    /**
     * The user for an email token is already verified
     */
    data class EmailTokenUserAlreadyVerified(val token: UUID) : ServiceException()

    /**
     * A book-invitation was not found
     */
    data class InvitationNotFound(val id: Long) : ServiceException()

    /**
     * A book-invitation cannot be accepted because it is not open anymore
     */
    data class InvitationNotOpen(val id: Long, val status: InvitationStatus) : ServiceException()

    /**
     * A book-invitation cannot be accepted because it is expired
     */
    data class InvitationExpired(val id: Long) : ServiceException()

    /**
     * An invitation for a specific user to a specific book is already open at the moment, thus another cannot be added
     */
    data class OpenInvitationAlreadyPresent(val userId: Long, val bookId: Long) : ServiceException()

}