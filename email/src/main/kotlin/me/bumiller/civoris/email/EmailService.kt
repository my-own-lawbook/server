package me.bumiller.civoris.email

import me.bumiller.civoris.model.BookInvitation
import me.bumiller.civoris.model.TwoFactorToken
import me.bumiller.civoris.model.User

/**
 * Interface to control the sending of preconfigured emails
 */
interface EmailService {

    /**
     * Sends an email containing an email-validation-token
     *
     * @param user The recipient. [User.email] is used as the sending address
     * @param token The token to verify the email
     */
    suspend fun sendEmailVerifyEmail(user: User, token: TwoFactorToken)

    /**
     * Sends an email reflecting the latest status change of it.
     *
     * @param invitation The invitation whose status was just updated
     */
    suspend fun sendInvitationStatusChangeEmail(invitation: BookInvitation)

}