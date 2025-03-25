package me.bumiller.civoris.email

import kotlinx.html.DIV
import me.bumiller.civoris.email.html.emailBase
import me.bumiller.civoris.email.html.emailVerifyContent
import me.bumiller.civoris.email.html.invitationStatusUpdate
import me.bumiller.civoris.model.BookInvitation
import me.bumiller.civoris.model.InvitationStatus
import me.bumiller.civoris.model.TwoFactorToken
import me.bumiller.civoris.model.User
import me.bumiller.civoris.model.config.AppConfig
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.ImageHtmlEmail
import org.apache.commons.mail.resolver.DataSourceUrlResolver
import java.net.URI

internal class ApacheEmailService(
    private val appConfig: AppConfig
) : EmailService {

    private val linkGenerator = appConfig.webBaseUrl?.let(::LinkGenerator)

    companion object {

        private const val EMAIL_FROM = "Civoris"

        private const val SUBJECT_EMAIL_VERIFY = "Verify your Email"
        private const val SUBJECT_INVITATION_NEW = "New invitation"
        private const val SUBJECT_INVITATION_REVOKED = "Invitation revoked"
        private const val SUBJECT_INVITATION_DECLINED = "Invitation declined"
        private const val SUBJECT_INVITATION_ACCEPTED = "Invitation accepted"

        private const val RELATIVE_LOGO_URL = "/civoris/assets/refs/heads/main/general/text_logo.png"
        private const val ASSETS_BASE_URL = "https://raw.githubusercontent.com"
        private const val GITHUB_URL = "https://github.com/civoris/"

        private const val GITHUB_LINK_PLACEHOLDER = "github_link"
        private const val WEB_LINK_PLACEHOLDER = "web_link"
        private const val OTP_PLACEHOLDER = "otp"
        private const val EMAIL_VERIFY_LINK_PLACEHOLDER = "email_verify_link"
        private const val USERNAME_PLACEHOLDER = "username"
        private const val TARGET_BOOK_NAME_PLACEHOLDER = "book_name"
        private const val AUTHOR_NAME_PLACEHOLDER = "author_name"
        private const val INVITATION_MEMBER_ROLE_PLACEHOLDER = "member_role"
        private const val INVITATION_EXPIRATION_PLACEHOLDER = "expiration"
        private const val INVITATION_MESSAGE_PLACEHOLDER = "message"
        private const val INVITATION_RECIPIENT_PLACEHOLDER = "recipient_name"
        private const val INVITATION_REVOKER_PLACEHOLDER = "revoker_name"

        private const val EMAIL_VERIFY_TEMPLATE = "email-verify"
        private const val INVITATION_NEW_TEMPLATE = "invitation-notification"
        private const val INVITATION_ACCEPTED_TEMPLATE = "invitation-accepted"
        private const val INVITATION_DENIED_TEMPLATE = "invitation-denied"
        private const val INVITATION_REVOKED_TEMPLATE = "invitation-revoked"

    }

    private val basePlaceholders = mapOf(
        GITHUB_LINK_PLACEHOLDER to GITHUB_URL,
        WEB_LINK_PLACEHOLDER to appConfig.webBaseUrl
    )

    override suspend fun sendEmailVerifyEmail(user: User, token: TwoFactorToken) {
        val html = createHtml {
            emailVerifyContent(
                otp = token.token, emailVerifyLink = linkGenerator?.emailVerify(token.token)
            )
        }

        createHtmlMail(user.email, html).apply { subject = SUBJECT_EMAIL_VERIFY }.send()
    }

    override suspend fun sendInvitationStatusChangeEmail(invitation: BookInvitation) {
        val html = createHtml {
            invitationStatusUpdate(
                invitation = invitation, webLink = linkGenerator?.invitationStatusUpdate(invitation)
            )
        }

        val recipient = when (invitation.status) {
            InvitationStatus.Open, InvitationStatus.Revoked -> invitation.recipient

            InvitationStatus.Accepted, InvitationStatus.Declined -> invitation.author
        }

        createHtmlMail(recipient.email, html)
            .apply { subject = invitation.status.subject() }
    }

    private fun createHtml(content: DIV.() -> Unit): String = StringBuilder().apply {
        emailBase(
            logoSrc = RELATIVE_LOGO_URL,
            githubLink = GITHUB_URL,
            webLink = appConfig.webBaseUrl,
            content = content
        )
    }.toString()

    private fun InvitationStatus.subject() = when (this) {
        InvitationStatus.Open -> SUBJECT_INVITATION_NEW
        InvitationStatus.Accepted -> SUBJECT_INVITATION_ACCEPTED
        InvitationStatus.Declined -> SUBJECT_INVITATION_DECLINED
        InvitationStatus.Revoked -> SUBJECT_INVITATION_REVOKED
    }

    private fun createHtmlMail(
        recipient: String, html: String
    ) = ImageHtmlEmail().apply {
        hostName = appConfig.mailSmtpServer
        authenticator = DefaultAuthenticator(appConfig.mailUsername, appConfig.mailPassword)
        isSSLOnConnect = appConfig.mailDoSsl

        setFrom("$EMAIL_FROM <${appConfig.mailUsername}>")
        setSmtpPort(appConfig.mailSmtpPort)

        addTo(recipient)

        setHtmlMsg(html)
        dataSourceResolver = DataSourceUrlResolver(URI.create(ASSETS_BASE_URL).toURL())
    }
}