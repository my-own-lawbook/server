package me.bumiller.civoris.email

import me.bumiller.civoris.email.formatting.format
import me.bumiller.civoris.email.formatting.formatFullName
import me.bumiller.civoris.email.formatting.formatName
import me.bumiller.civoris.email.template.TemplateResult
import me.bumiller.civoris.email.template.generateTemplate
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

    companion object {

        private const val EMAIL_FROM = "Civoris"

        private const val SUBJECT_EMAIL_VERIFY = "Verify your Email"
        private const val SUBJECT_INVITATION_NEW = "New invitation"
        private const val SUBJECT_INVITATION_REVOKED = "Invitation revoked"
        private const val SUBJECT_INVITATION_DECLINED = "Invitation declined"
        private const val SUBJECT_INVITATION_ACCEPTED = "Invitation accepted"

        private const val ASSETS_BASE_URL = "https://raw.githubusercontent.com"
        private const val GITHUB_URL = "https://github.com/civoris/"

        private const val GITHUB_LINK_PLACEHOLDER = "github_link"
        private const val WEB_LINK_PLACEHOLDER = "web_link"
        private const val OTP_PLACEHOLDER = "otp"
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
        val placeholders = mapOf(
            OTP_PLACEHOLDER to token.token
        )

        createHtmlMail(
            recipient = user.email,
            templateName = EMAIL_VERIFY_TEMPLATE,
            basePlaceholders + placeholders
        ).apply {
            subject = SUBJECT_EMAIL_VERIFY
        }.send()
    }

    override suspend fun sendInvitationStatusChangeEmail(invitation: BookInvitation) {
        when (invitation.status) {
            InvitationStatus.Open -> invitationCreatedEmail(invitation)
            InvitationStatus.Accepted -> invitationAcceptedMail(invitation)
            InvitationStatus.Declined -> invitationDeniedMail(invitation)
            InvitationStatus.Revoked -> invitationRevokedMail(invitation)
        }.apply {
            subject = invitation.status.subject()
        }.send()
    }

    private fun InvitationStatus.subject() = when (this) {
        InvitationStatus.Open -> SUBJECT_INVITATION_NEW
        InvitationStatus.Accepted -> SUBJECT_INVITATION_ACCEPTED
        InvitationStatus.Declined -> SUBJECT_INVITATION_DECLINED
        InvitationStatus.Revoked -> SUBJECT_INVITATION_REVOKED
    }

    private fun invitationAcceptedMail(invitation: BookInvitation) = createHtmlMail(
        recipient = invitation.author.email,
        templateName = INVITATION_ACCEPTED_TEMPLATE,
        placeholders = basePlaceholders + mapOf(
            USERNAME_PLACEHOLDER to invitation.author.username,
            INVITATION_RECIPIENT_PLACEHOLDER to invitation.recipient.formatFullName(),
            TARGET_BOOK_NAME_PLACEHOLDER to invitation.targetBook.name
        )
    )

    private fun invitationRevokedMail(invitation: BookInvitation) = createHtmlMail(
        recipient = invitation.recipient.email,
        templateName = INVITATION_REVOKED_TEMPLATE,
        placeholders = basePlaceholders + mapOf(
            USERNAME_PLACEHOLDER to invitation.recipient.username,
            TARGET_BOOK_NAME_PLACEHOLDER to invitation.targetBook.name
        )
    )

    private fun invitationDeniedMail(invitation: BookInvitation) = createHtmlMail(
        recipient = invitation.author.email,
        templateName = INVITATION_DENIED_TEMPLATE,
        placeholders = basePlaceholders + mapOf(
            USERNAME_PLACEHOLDER to invitation.author.username,
            INVITATION_RECIPIENT_PLACEHOLDER to invitation.recipient.formatFullName(),
            TARGET_BOOK_NAME_PLACEHOLDER to invitation.targetBook.name
        )
    )

    private fun invitationCreatedEmail(invitation: BookInvitation) = createHtmlMail(
        recipient = invitation.recipient.email,
        templateName = INVITATION_NEW_TEMPLATE,
        placeholders = basePlaceholders + mapOf(
            USERNAME_PLACEHOLDER to invitation.recipient.username,
            AUTHOR_NAME_PLACEHOLDER to invitation.author.formatFullName(),
            TARGET_BOOK_NAME_PLACEHOLDER to invitation.targetBook.name,
            INVITATION_MEMBER_ROLE_PLACEHOLDER to invitation.role.formatName(),
            INVITATION_EXPIRATION_PLACEHOLDER to invitation.expiredAt?.format(),
            INVITATION_MESSAGE_PLACEHOLDER to invitation.message
        )
    )

    private fun createHtmlMail(
        recipient: String,
        templateName: String,
        placeholders: Map<String, String?>
    ) = ImageHtmlEmail().apply {
        hostName = appConfig.mailSmtpServer
        authenticator = DefaultAuthenticator(appConfig.mailUsername, appConfig.mailPassword)
        isSSLOnConnect = appConfig.mailDoSsl

        setFrom("$EMAIL_FROM <${appConfig.mailUsername}>")
        setSmtpPort(appConfig.mailSmtpPort)

        addTo(recipient)

        val template = generateTemplate(templateName, placeholders)
        val html = when (template) {
            is TemplateResult.Template<String> -> template.html
            is TemplateResult.MissingPlaceholder<String> -> throw IllegalStateException("Could not create template due to missing placeholder: '${template.missingKey}'.")
        }

        setHtmlMsg(html)
        dataSourceResolver = DataSourceUrlResolver(URI.create(ASSETS_BASE_URL).toURL())
    }
}