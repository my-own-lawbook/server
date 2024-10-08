package me.bumiller.mol.email

import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.User
import me.bumiller.mol.model.config.AppConfig
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail

internal class ApacheEmailService(
    private val appConfig: AppConfig
) : EmailService {

    override suspend fun sendEmailVerifyEmail(user: User, token: TwoFactorToken) {
        baseEmail(user.email).apply {
            subject = "Verify your email address"
            setMsg(
                """
            Hello,
            
            to confirm your email address, use the following token:
            
            '${token.token}'.
        """.trimIndent()
            )
        }.send()
    }

    override suspend fun sendPasswordResetEmail(user: User, token: TwoFactorToken) {
        requireNotNull(user.profile)

        baseEmail(user.email).apply {
            subject = "Reset password token"
            setMsg(
                """
            Hello ${user.profile!!.firstName} ${user.profile!!.lastName},
            
            to reset your password, use the following token:
            
            '${token.token}'.
        """.trimIndent()
            )
        }.send()
    }

    private fun baseEmail(recipient: String) = SimpleEmail()
        .apply {
            hostName = appConfig.mailSmtpServer
            authenticator = DefaultAuthenticator(appConfig.mailUsername, appConfig.mailPassword)
            isSSLOnConnect = appConfig.mailDoSsl

            setFrom(appConfig.mailUsername)
            setSmtpPort(appConfig.mailSmtpPort)

            addTo(recipient)
        }
}