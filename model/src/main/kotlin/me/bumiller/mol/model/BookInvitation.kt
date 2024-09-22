package me.bumiller.mol.model

import kotlinx.datetime.Instant

/**
 * Models an invitation from one user to another into a law-book
 */
data class BookInvitation(

    /**
     * The id
     */
    val id: Long,

    /**
     * The user that sent the invitation
     */
    val author: User,

    /**
     * The target of the invitation
     */
    val targetBook: LawBook,

    /**
     * The user receiving the invitation
     */
    val recipient: User,

    /**
     * The role applied to the user in the case of acceptance
     */
    val role: MemberRole,

    /**
     * The timestamp when the invitation was sent
     */
    val sentAt: Instant,

    /**
     * The timestamp at which [status] changed from [InvitationStatus.Open] to any other status.
     */
    val usedAt: Instant?,

    /**
     * The status of the invitation
     */
    val status: InvitationStatus,

    /**
     * Time of expiry, or null if it doesn't expire
     */
    val expiredAt: Instant?,

    /**
     * Optional message
     */
    val message: String?

)
