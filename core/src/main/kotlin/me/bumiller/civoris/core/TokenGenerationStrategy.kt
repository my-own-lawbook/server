package me.bumiller.civoris.core

/**
 * Different strategies of generating a token for a TwoFactorToken
 */
sealed interface TokenGenerationStrategy {

    /**
     * Simple UUid token.
     */
    data object UUID : TokenGenerationStrategy

    /**
     * Numerical one time password with a specific count of letters.
     *
     * @param length The length of the token
     */
    data class OneTimePassword(val length: Int) : TokenGenerationStrategy

}