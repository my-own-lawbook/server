package me.bumiller.civoris.email

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

        private const val ASSETS_BASE_URL = "https://raw.githubusercontent.com"

        private const val OTP = "otp"

        private const val PLACEHOLDER_PREFIX = "{{"
        private const val PLACEHOLDER_POSTFIX = "}}"

    }

    private val emailVerifyHtmlContent: String

    init {
        emailVerifyHtmlContent =
            this::class.java.getResourceAsStream("/templates/email_verify.html")
                ?.bufferedReader()
                ?.readLines()
                ?.joinToString(System.lineSeparator())
                ?: throw IllegalStateException("Could not read html resource")
    }

    override suspend fun sendEmailVerifyEmail(user: User, token: TwoFactorToken) {
        val html = emailVerifyHtmlContent
            .placeholder(OTP, token.token)

        createHtmlMail(user.email, html).apply {
            subject = "Verify your email address"
        }
            .send()
    }

    override suspend fun sendPasswordResetEmail(user: User, token: TwoFactorToken) {
        TODO()
    }

    private fun createHtmlMail(
        recipient: String,
        htmlContent: String
    ) = ImageHtmlEmail().apply {
        hostName = appConfig.mailSmtpServer
        authenticator = DefaultAuthenticator(appConfig.mailUsername, appConfig.mailPassword)
        isSSLOnConnect = appConfig.mailDoSsl

        setFrom(appConfig.mailUsername)
        setSmtpPort(appConfig.mailSmtpPort)

        addTo(recipient)

        setHtmlMsg(htmlContent)
        dataSourceResolver = DataSourceUrlResolver(URI.create(ASSETS_BASE_URL).toURL())
    }

    private fun String.placeholder(placeholder: String, value: String) =
        replace(PLACEHOLDER_PREFIX + placeholder + PLACEHOLDER_POSTFIX, value)
}