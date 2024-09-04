package me.bumiller.mol.model

/**
 * Types that a [TwoFactorToken] can be used for
 */
enum class TwoFactorTokenType(

    /**
     * Name for storing in string form
     */
    val serializedName: String

) {

    /**
     * Confirming an email
     */
    EmailConfirm("email_confirm"),

    /**
     * Resetting the password
     */
    PasswordReset("password_reset"),

    /**
     * Used as a refresh token
     */
    RefreshToken("refresh_token")

}