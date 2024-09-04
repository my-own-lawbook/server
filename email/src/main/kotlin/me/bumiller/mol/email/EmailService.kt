package me.bumiller.mol.email

import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.User

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
     * Sends an email containing a password-reset-token
     *
     * @param user The recipient. [User.email] is used as the sending address
     * @param token The token to verify the email
     */
    suspend fun sendPasswordResetEmail(user: User, token: TwoFactorToken)

}