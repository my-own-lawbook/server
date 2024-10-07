package me.bumiller.mol.core.impl

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.bumiller.mol.core.data.InvitationContentService
import me.bumiller.mol.core.exception.ServiceException
import me.bumiller.mol.core.mapping.mapInvitation
import me.bumiller.mol.core.mapping.mapStatus
import me.bumiller.mol.core.mapping.memberRoleToString
import me.bumiller.mol.database.repository.BookInvitationRepository
import me.bumiller.mol.database.repository.LawBookRepository
import me.bumiller.mol.database.repository.UserRepository
import me.bumiller.mol.model.BookInvitation
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.model.MemberRole

internal class DatabaseInvitationContentService(
    private val invitationRepository: BookInvitationRepository,
    private val userRepository: UserRepository,
    private val bookRepository: LawBookRepository
) : InvitationContentService() {

    override suspend fun getInvitationById(id: Long) = invitationRepository
        .getSpecific(id)
        ?.let(::mapInvitation) ?: throw ServiceException.InvitationNotFound(id)

    override suspend fun getAll(
        authorId: Long?,
        targetBookId: Long?,
        recipientId: Long?,
        statuses: List<InvitationStatus>,
        onlyNonExpired: Boolean
    ) = invitationRepository
        .getAll(
            authorId = authorId,
            targetBookId = targetBookId,
            recipientId = recipientId,
            statuses = statuses.map(::mapStatus),
            onlyNonExpired = onlyNonExpired
        ).map(::mapInvitation)

    override suspend fun createInvitation(
        authorId: Long,
        targetBookId: Long,
        recipientId: Long,
        role: MemberRole,
        expiresAt: Instant?,
        message: String?
    ): BookInvitation {
        val author = userRepository.getSpecific(authorId) ?: throw ServiceException.UserNotFound(authorId)
        val book = bookRepository.getSpecific(targetBookId) ?: throw ServiceException.LawBookNotFound(targetBookId)
        val recipient = userRepository.getSpecific(recipientId) ?: throw ServiceException.UserNotFound(recipientId)

        val invitation = invitationRepository.create(
            model = me.bumiller.mol.database.table.BookInvitation.Model(
                id = -1,
                author = author,
                targetBook = book,
                recipient = recipient,
                role = memberRoleToString(role),
                sentAt = Clock.System.now(),
                usedAt = null,
                status = me.bumiller.mol.database.table.BookInvitation.Status.Open,
                expiresAt = expiresAt,
                message = message
            ),
            authorId = author.id,
            targetBookId = book.id,
            recipientId = recipient.id
        )

        return invitation!!
            .let(::mapInvitation)
    }

    override suspend fun updateStatus(invitationId: Long, status: InvitationStatus) {
        val invitation =
            invitationRepository.getSpecific(invitationId) ?: throw ServiceException.InvitationNotFound(invitationId)
        val updated = invitation.copy(
            status = mapStatus(status),
            usedAt = Clock.System.now()
        )

        invitationRepository
            .update(updated)
    }

}