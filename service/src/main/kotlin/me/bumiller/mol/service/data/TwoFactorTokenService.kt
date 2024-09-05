package me.bumiller.mol.service.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.bumiller.mol.model.TwoFactorToken
import me.bumiller.mol.model.TwoFactorTokenType
import java.util.*

/**
 * Interface to perform common operations on two factor tokens
 */
interface TwoFactorTokenService {

    /**
     * Gets all two factor tokens
     */
    suspend fun getAll(): List<TwoFactorToken>

    /**
     * Gets a specific token from the database
     *
     * @param id The id of the token
     * @param token The token uuid
     * @return A two factor token matching all given non-null criteria
     */
    suspend fun getSpecific(id: Long? = null, token: UUID? = null): TwoFactorToken?

    /**
     * Creates a new two factor token in the database
     *
     * @param type The type of token
     * @param userId The id of the user to create the token for
     * @param expiringAt Until when the token is valid
     * @param issuedAt The time of creation stored in the token
     * @param additionalContent Optional additional metadata stored in the token
     * @return The created token, or null if the user for [userId] was not found
     */
    suspend fun create(
        type: TwoFactorTokenType,
        userId: Long,
        expiringAt: Instant? = null,
        issuedAt: Instant = Clock.System.now(),
        additionalContent: String? = null
    ): TwoFactorToken?

    /**
     * Marks a specific token as used.
     *
     * @param tokenId The id of the token to mark as used
     * @return The token for [tokenId], or null if it was not found
     */
    suspend fun markAsUsed(tokenId: Long): TwoFactorToken?

}