package me.bumiller.civoris.email.html

import kotlinx.html.DIV
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style

private const val OTP_AREA_STYLE = """
    background: #E7E8EE;
    border-radius: 32px;
    padding-left: 32px;
    padding-right: 32px;
    color: #415F91;
"""

/**
 * Email content for the email that asks the user to verify their email address.
 *
 * @param otp The one-time-password
 * @param emailVerifyLink The link to verify their email address with, or null
 */
fun DIV.emailVerifyContent(
    otp: String,
    emailVerifyLink: String?
) {
    val isLinkAvailable = emailVerifyLink != null
    val paragraphContent = if (isLinkAvailable) "...or use the following OTP to verify the E-mail in app:"
    else "Welcome to Civoris! To verify your email address, use the following OTP code:"

    emailTitle("Verify your email address")

    if (isLinkAvailable) {
        p {
            +"Welcome to Civoris! To verify your email address, click the button below..."
        }

        centered {
            webLinkButton(
                text = "Verify E-mail",
                webLink = emailVerifyLink!!
            )
        }
    }

    p { +paragraphContent }

    centered {
        otpArea(otp)
    }

    emailEnd()

}

private fun DIV.otpArea(
    otp: String
) {
    centered {
        style = OTP_AREA_STYLE

        h1 {
            +otp
        }
    }
}