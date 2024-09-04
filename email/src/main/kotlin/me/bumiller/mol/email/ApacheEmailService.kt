package me.bumiller.mol.email

import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.User
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import java.util.*

internal class ApacheEmailService : EmailService {

    val properties = Properties().apply {
        val stream = javaClass.getResource("/email/email.properties")?.openStream()
            ?: throw IllegalStateException("Did not find the /email/email.properties file!")
        load(stream)
    }

    override suspend fun sendEmailVerifyEmail(user: User, token: TwoFactorToken) {
        require(user.profile != null)

        baseEmail(user.email).apply {
            subject = "Verify your email address"
            setMsg(
                """
            Hello ${user.profile!!.firstName} ${user.profile!!.lastName},
            
            to confirm your email address, use the following token:
            
            '${token.token}'.
        """.trimIndent()
            )
        }.send()
    }

    override suspend fun sendPasswordResetEmail(user: User, token: TwoFactorToken) {
        require(user.profile != null)

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
            hostName = properties.getProperty("host")
            authenticator = DefaultAuthenticator(properties.getProperty("from"), properties.getProperty("password"))

            setFrom(properties.getProperty("from"))
            setSmtpPort(properties.getProperty("port").toInt())

            addTo(recipient)
        }
}