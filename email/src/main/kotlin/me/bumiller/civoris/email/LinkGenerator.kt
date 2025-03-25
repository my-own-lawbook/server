package me.bumiller.civoris.email

import io.ktor.http.*
import me.bumiller.civoris.model.BookInvitation
import me.bumiller.civoris.model.InvitationStatus

/**
 * Class responsible for generating the links in emails.
 */
class LinkGenerator(

    /**
     * The base url of the frontent
     */
    private val baseUrl: String

) {

    companion object {

        private const val BASE_PATH = "l/"
        private const val QUERY_PREFIX = "?"
        private const val QUERY_DELIMITER = "="

        private const val OTP_QUERY_NAME = "otp"
        private const val INVITATION_ID_NAME = "id"

        private const val EMAIL_VERIFY_PATH = "email-verify"
        private const val INVITATION_NEW_PATH = "invitation-new"
        private const val INVITATION_DENIED_PATH = "invitation-denied"
        private const val INVITATION_ACCEPTED_PATH = "invitation-accepted"
        private const val INVITATION_REVOKED_PATH = "invitation-revoked"

    }

    /**
     * Builds the link for the email verification email.
     *
     * @param otp The one time password from the email
     * @return The url
     */
    fun emailVerify(otp: String) = url(EMAIL_VERIFY_PATH) {
        parameters.append(OTP_QUERY_NAME, otp)
    }

    /**
     * Build the link for the emails that deal with an updated invitation status.
     *
     * @param invitation The new invitation
     * @return The link
     */
    fun invitationStatusUpdate(invitation: BookInvitation) = url(invitation.status.path()) {
        parameters.append(INVITATION_ID_NAME, invitation.id.toString())
    }

    private fun url(path: String, builder: URLBuilder.() -> Unit) =
        URLBuilder(baseUrl + BASE_PATH + path)
            .apply(builder)
            .build()
            .toString()

    private fun InvitationStatus.path() =
        when (this) {
            InvitationStatus.Open -> INVITATION_NEW_PATH
            InvitationStatus.Accepted -> INVITATION_ACCEPTED_PATH
            InvitationStatus.Declined -> INVITATION_DENIED_PATH
            InvitationStatus.Revoked -> INVITATION_REVOKED_PATH
        }

}