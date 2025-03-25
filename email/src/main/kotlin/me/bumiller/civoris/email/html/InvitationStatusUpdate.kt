package me.bumiller.civoris.email.html

import kotlinx.html.*
import me.bumiller.civoris.email.formatting.format
import me.bumiller.civoris.email.formatting.formatFullName
import me.bumiller.civoris.email.formatting.formatName
import me.bumiller.civoris.model.BookInvitation
import me.bumiller.civoris.model.InvitationStatus

private const val INVITATION_INFO_BOX_STYLE = """
    background: #E7E8EE;
    border-radius: 32px;
    padding: 16px;
    color: #415F91;
    display: grid;
    grid-template-columns: min-content 1fr;
"""

/**
 * Email content for the email that displays information about a changes status of an invitation.
 *
 * @param invitation The invitation
 * @param emailVerifyLink The link to verify their email address with, or null
 */
fun DIV.invitationStatusUpdate(
    invitation: BookInvitation,
    webLink: String?
) {
    when (invitation.status) {
        InvitationStatus.Open -> invitationReceiveEmail(invitation)
        InvitationStatus.Accepted -> invitationAccepted(invitation)
        InvitationStatus.Declined -> invitationDeclined(invitation)
        InvitationStatus.Revoked -> invitationRevoked(invitation)
    }

    webLink?.let {
        webLinkButton(
            text = "View",
            webLink = it
        )
    }

    emailEnd()
}

private fun DIV.invitationDeclined(
    invitation: BookInvitation
) {
    emailTitle("Invitation declined")

    p { +"Hello ${invitation.author.formatFullName()}," }

    p {
        +"Your invitation for"
        b { +invitation.recipient.formatFullName() }
        +" into the book"
        b { +invitation.targetBook.name }
        +"has been declined."
    }
}

private fun DIV.invitationAccepted(
    invitation: BookInvitation
) {
    emailTitle("Invitation accepted")

    p { +"Hello ${invitation.author.formatFullName()}," }

    p {
        +"Your invitation for"
        b { +invitation.recipient.formatFullName() }
        +" into the book"
        b { +invitation.targetBook.name }
        +"has been accepted."
    }
}

private fun DIV.invitationRevoked(
    invitation: BookInvitation
) {
    emailTitle("Invitation revoked")

    p { +"Hello ${invitation.recipient.formatFullName()}," }

    p {
        +"Your invitation from"
        b { +invitation.recipient.formatFullName() }
        +" into the book"
        b { +invitation.targetBook.name }
        +"has been revoked."
    }
}

private fun DIV.invitationReceiveEmail(
    invitation: BookInvitation
) {
    emailTitle("New invitation")

    p { +"Hello ${invitation.recipient.formatFullName()}," }

    p {
        +"You habe been invited into the book "
        b { +invitation.targetBook.name }
        +" by "
        b { +invitation.author.formatFullName() }
        +"."
    }

    invitationInfoBox(invitation)
}

private fun DIV.invitationInfoBox(
    invitation: BookInvitation
) {
    div {
        style = INVITATION_INFO_BOX_STYLE

        invitationInfoBoxItem("Member role:", invitation.role.formatName())

        invitation.expiredAt?.let {
            invitationInfoBoxItem("Expiring at:", it.format())
        }
        invitation.message?.let {
            invitationInfoBoxItem("Message:", it)
        }
    }
}

private fun DIV.invitationInfoBoxItem(
    name: String,
    value: String
) {
    p { +name }
    p { +value }
}