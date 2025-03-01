package me.bumiller.civoris.model

/**
 * Data class combining two types of tokens
 */
data class AuthTokens(

    /**
     * The JWT token for accessing the API
     */
    val jwt: String,

    /**
     * The refresh token for issuing more jwt tokens
     */
    val refresh: TwoFactorToken

)