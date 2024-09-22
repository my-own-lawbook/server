package me.bumiller.mol.core.data

import kotlinx.datetime.Instant
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.BookInvitation
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.MemberRole

/**
 * Service to perform basic operations and queries on the invitations
 */
abstract class InvitationContentService {

    /**
     * Gets a specific invitation
     *
     * @param id The id of the invitation
     * @return The invitation
     * @throws ServiceException.InvitationNotFound If the invitation for [id] was not found
     */
    abstract suspend fun getInvitationById(id: Long): BookInvitation

    /**
     * Gets all invitations matching all given criteria
     *
     * @param authorId The id of the author
     * @param targetBookId The id of the target book
     * @param recipientId The id of the target user
     * @param statuses The statuses to filter by
     * @param onlyNonExpired Whether to filter out expired ones
     */
    abstract suspend fun getAll(
        authorId: Long? = null,
        targetBookId: Long? = null,
        recipientId: Long? = null,
        statuses: List<InvitationStatus> = InvitationStatus.entries,
        onlyNonExpired: Boolean = false
    ): List<BookInvitation>

    /**
     * Creates a new invitation
     *
     * @param authorId The id of the author
     * @param targetBookId The id of the book
     * @param recipientId The id of the recipient
     * @param role The role
     * @param expiresAt The optional expiration date
     * @param message The optional message
     * @throws ServiceException.UserNotFound If the author or the recipient could not be found
     * @throws ServiceException.LawBookNotFound If the book could not be found
     */
    internal abstract suspend fun createInvitation(
        authorId: Long,
        targetBookId: Long,
        recipientId: Long,
        role: MemberRole = MemberRole.Member,
        expiresAt: Instant? = null,
        message: String? = null
    ): BookInvitation

    /**
     * Updates the invitation status of a specific invitation
     *
     * @param invitationId The id of the invitation
     * @param status The new status
     * @throws ServiceException.InvitationNotFound
     */
    internal abstract suspend fun updateStatus(invitationId: Long, status: InvitationStatus)

}