package me.bumiller.mol.model

import com.auth0.jwt.JWT

/**
 * Data class combining two types of tokens
 */
data class AuthTokens(

    /**
     * The JWT token for accessing the API
     */
    val jwt: JWT,

    /**
     * The refresh token for issuing more jwt tokens
     */
    val refresh: TwoFactorToken

)