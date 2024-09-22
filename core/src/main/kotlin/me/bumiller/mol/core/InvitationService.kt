package me.bumiller.mol.core

import kotlinx.datetime.Instant
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.BookInvitation
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.MemberRole

/**
 * Service to perform common operations on book-invitations
 */
interface InvitationService {

    /**
     * Creates a new invitation.
     *
     * Also checks whether the author is able to create an invitation for the book
     *
     * @param authorId The id of the author
     * @param targetBookId The id of the book
     * @param recipientId The id of the recipient
     * @param role The role
     * @param expiresAt The optional expiration date
     * @param message The optional message
     * @throws ServiceException.UserNotFound If the author or the recipient could not be found
     * @throws ServiceException.LawBookNotFound If the book could not be found
     * @throws ServiceException.UserNotMemberOfBook If the author is not a member of the book
     * @throws ServiceException.UserInvalidRoleInBook If the author does not have the required role to create a permission
     * @throws ServiceException.OpenInvitationAlreadyPresent If an invitation for the recipient to the target book is already open
     * @throws ServiceException.UserAlreadyMemberOfBook If the user is already a member of the book
     */
    suspend fun createInvitation(
        authorId: Long,
        targetBookId: Long,
        recipientId: Long,
        role: MemberRole = MemberRole.Member,
        expiresAt: Instant? = null,
        message: String? = null
    ): BookInvitation

    /**
     * Accepts a given invitation.
     *
     * Also adds the appropriate user to the targeted book and applies the given role.
     *
     * @param invitationId The id of the invitation
     * @throws ServiceException.InvitationNotFound If the invitation was not found
     * @throws ServiceException.InvitationNotOpen If the invitation status is not [InvitationStatus.Open] anymore
     * @throws ServiceException.InvitationExpired If the invitation is expired
     */
    suspend fun acceptInvitation(invitationId: Long)

    /**
     * Denies a given invitation
     *
     * @param invitationId The id of the invitation
     * @throws ServiceException.InvitationExpired If the invitation was not found
     * @throws ServiceException.InvitationNotOpen If the invitation status is not [InvitationStatus.Open] anymore
     */
    suspend fun denyInvitation(invitationId: Long)

    /**
     * Revokes a given invitation
     *
     * @param invitationId The id of the invitation
     * @throws ServiceException.InvitationExpired If the invitation was not found
     * @throws ServiceException.InvitationNotOpen If the invitation status is not [InvitationStatus.Open] anymore
     */
    suspend fun revokeInvitation(invitationId: Long)

}