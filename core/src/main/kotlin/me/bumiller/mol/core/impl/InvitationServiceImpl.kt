package me.bumiller.mol.core.impl

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.bumiller.mol.core.InvitationService
import me.bumiller.mol.core.data.InvitationContentService
import me.bumiller.mol.core.data.MemberContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.model.BookInvitation
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.MemberRole
import me.bumiller.mol.model.User

internal class InvitationServiceImpl(
    private val invitationContentService: InvitationContentService,
    private val memberContentService: MemberContentService
) : InvitationService {

    override suspend fun createInvitation(
        authorId: Long,
        targetBookId: Long,
        recipientId: Long,
        role: MemberRole,
        expiresAt: Instant?,
        message: String?
    ): BookInvitation {
        val memberRole = memberContentService.getMemberRole(authorId, targetBookId)

        if (!(memberRole satisfies MemberRole.Admin)) throw ServiceException.UserInvalidRoleInBook(
            authorId,
            targetBookId,
            memberRole,
            MemberRole.Admin
        )

        val membersForBook = memberContentService.getMembersInBook(targetBookId)
        val userInBook = recipientId in membersForBook.map(User::id)
        if (userInBook) throw ServiceException.UserAlreadyMemberOfBook(recipientId, targetBookId)

        val rolesForUserAndBook = invitationContentService
            .getAll(
                targetBookId = targetBookId,
                recipientId = recipientId,
                statuses = listOf(InvitationStatus.Open),
                onlyNonExpired = true
            )
        if (rolesForUserAndBook.isNotEmpty()) throw ServiceException.OpenInvitationAlreadyPresent(
            recipientId,
            targetBookId
        )

        return invitationContentService.createInvitation(authorId, targetBookId, recipientId, role, expiresAt, message)
    }

    override suspend fun acceptInvitation(invitationId: Long) {
        val invitation = invitationContentService.getInvitationById(invitationId)

        val notOpen = invitation.status != InvitationStatus.Open
        if (notOpen)
            throw ServiceException.InvitationNotOpen(invitationId, invitation.status)

        val expired = invitation.expiredAt?.let { it < Clock.System.now() } ?: false
        if (expired)
            throw ServiceException.InvitationExpired(invitationId)

        invitationContentService.updateStatus(invitationId, InvitationStatus.Accepted)

        memberContentService.addMemberToBook(invitation.targetBook.id, invitation.recipient.id)
        memberContentService.setMemberRole(invitation.recipient.id, invitation.targetBook.id, invitation.role)
    }

    override suspend fun denyInvitation(invitationId: Long) {
        val invitation = invitationContentService.getInvitationById(invitationId)

        val notOpen = invitation.status != InvitationStatus.Open
        if (notOpen)
            throw ServiceException.InvitationNotOpen(invitationId, invitation.status)

        invitationContentService.updateStatus(invitationId, InvitationStatus.Declined)
    }

    override suspend fun revokeInvitation(invitationId: Long) {
        val invitation = invitationContentService.getInvitationById(invitationId)

        val notOpen = invitation.status != InvitationStatus.Open
        if (notOpen)
            throw ServiceException.InvitationNotOpen(invitationId, invitation.status)

        invitationContentService.updateStatus(invitationId, InvitationStatus.Revoked)
    }
}