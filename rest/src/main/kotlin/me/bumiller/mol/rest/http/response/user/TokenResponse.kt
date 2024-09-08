package me.bumiller.mol.rest.http.response.user

import kotlinx.serialization.Serializable
import me.bumiller.mol.model.AuthTokens

/**
 * Response for endpoints that return an access (jwt) token and a refresh token
 */
@Serializable
data class TokenResponse (

    /**
     * The access token
     */
    val accessToken: String,

    /**
     * The refresh token
     */
    val refreshToken: String

) {

    companion object {

        fun create(tokens: AuthTokens) = TokenResponse(tokens.jwt, tokens.refresh.token.toString())

    }

}