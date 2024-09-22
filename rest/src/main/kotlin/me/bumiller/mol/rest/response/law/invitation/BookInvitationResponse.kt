package me.bumiller.mol.rest.response.law.invitation

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.bumiller.mol.model.BookInvitation
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.MemberRole

/**
 * Class that contains information about one specific book-invitation
 */
@Serializable
data class BookInvitationResponse(

    val id: Long,

    val authorId: Long,

    val targetBookId: Long,

    val recipientId: Long,

    val role: MemberRole,

    val sentAt: Instant,

    val usedAt: Instant?,

    val status: InvitationStatus,

    val expiredAt: Instant?,

    val message: String?

) {

    companion object {

        fun create(model: BookInvitation) = model.run {
            BookInvitationResponse(
                id,
                author.id,
                targetBook.id,
                recipient.id,
                role,
                sentAt,
                usedAt,
                status,
                expiredAt,
                message
            )
        }

    }

}
