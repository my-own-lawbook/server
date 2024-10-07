package me.bumiller.mol.core.mapping

import me.bumiller.mol.model.BookInvitation
import me.bumiller.mol.model.InvitationStatus
import me.bumiller.mol.database.table.BookInvitation.Model as BookInvitationModel

internal fun mapInvitation(model: BookInvitationModel) = BookInvitation(
    id = model.id,
    author = mapUser(model.author),
    targetBook = mapBook(model.targetBook),
    recipient = mapUser(model.recipient),
    role = memberRoleFromString(model.role),
    sentAt = model.sentAt,
    usedAt = model.usedAt,
    status = mapStatus(model.status),
    expiredAt = model.expiresAt,
    message = model.message
)

internal fun mapStatus(status: InvitationStatus) = when (status) {
    InvitationStatus.Open -> me.bumiller.mol.database.table.BookInvitation.Status.Open
    InvitationStatus.Accepted -> me.bumiller.mol.database.table.BookInvitation.Status.Accepted
    InvitationStatus.Declined -> me.bumiller.mol.database.table.BookInvitation.Status.Denied
    InvitationStatus.Revoked -> me.bumiller.mol.database.table.BookInvitation.Status.Revoked
}

private fun mapStatus(status: me.bumiller.mol.database.table.BookInvitation.Status) =
    when (status) {
        me.bumiller.mol.database.table.BookInvitation.Status.Open -> InvitationStatus.Open
        me.bumiller.mol.database.table.BookInvitation.Status.Accepted -> InvitationStatus.Accepted
        me.bumiller.mol.database.table.BookInvitation.Status.Denied -> InvitationStatus.Declined
        me.bumiller.mol.database.table.BookInvitation.Status.Revoked -> InvitationStatus.Revoked
    }