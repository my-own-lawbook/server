package me.bumiller.civoris.core.impl

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.bumiller.civoris.core.InvitationService
import me.bumiller.civoris.core.data.InvitationContentService
import me.bumiller.civoris.core.data.MemberContentService
import me.bumiller.civoris.core.exception.ServiceException
import me.bumiller.civoris.email.EmailService
import me.bumiller.civoris.model.BookInvitation
import me.bumiller.civoris.model.InvitationStatus
import me.bumiller.civoris.model.MemberRole
import me.bumiller.civoris.model.User

internal class InvitationServiceImpl(
    private val invitationContentService: InvitationContentService,
    private val memberContentService: MemberContentService,
    private val emailService: EmailService
) : InvitationService {

    override suspend fun createInvitation(
        authorId: Long,
        targetBookId: Long,
        recipientId: Long,
        role: MemberRole,
        expiresAt: Instant?,
        message: String?
    ): BookInvitation {
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
        println("Inside createInvitation")

        return invitationContentService.createInvitation(authorId, targetBookId, recipientId, role, expiresAt, message)
            .also { emailService.sendInvitationStatusChangeEmail(it) }
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

        val updatedInvitation = invitationContentService.getInvitationById(invitationId)
        emailService.sendInvitationStatusChangeEmail(updatedInvitation)
    }

    override suspend fun denyInvitation(invitationId: Long) {
        val invitation = invitationContentService.getInvitationById(invitationId)

        val notOpen = invitation.status != InvitationStatus.Open
        if (notOpen)
            throw ServiceException.InvitationNotOpen(invitationId, invitation.status)

        invitationContentService.updateStatus(invitationId, InvitationStatus.Declined)

        val updatedInvitation = invitationContentService.getInvitationById(invitationId)
        emailService.sendInvitationStatusChangeEmail(updatedInvitation)
    }

    override suspend fun revokeInvitation(invitationId: Long) {
        val invitation = invitationContentService.getInvitationById(invitationId)

        val notOpen = invitation.status != InvitationStatus.Open
        if (notOpen)
            throw ServiceException.InvitationNotOpen(invitationId, invitation.status)

        invitationContentService.updateStatus(invitationId, InvitationStatus.Revoked)

        val updatedInvitation = invitationContentService.getInvitationById(invitationId)
        emailService.sendInvitationStatusChangeEmail(updatedInvitation)
    }

    override suspend fun hasUserActiveInvitation(userId: Long, targetId: Long?): Boolean {
        val invitations = invitationContentService.getAll(
            recipientId = userId,
            targetBookId = targetId,
            statuses = listOf(InvitationStatus.Open),
            onlyNonExpired = true
        )

        return invitations.isNotEmpty()
    }
}