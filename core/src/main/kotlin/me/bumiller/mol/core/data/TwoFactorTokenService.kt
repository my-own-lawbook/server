package me.bumiller.mol.core.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.bumiller.mol.core.exception.ServiceException
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
     * @return The [TwoFactorToken]
     * @throws ServiceException.TwoFactorTokenNotFound If the token could not be found
     */
    suspend fun getSpecific(id: Long? = null, token: UUID? = null): TwoFactorToken

    /**
     * Creates a new two factor token in the database
     *
     * @param type The type of token
     * @param userId The id of the user to create the token for
     * @param expiringAt Until when the token is valid
     * @param issuedAt The time of creation stored in the token
     * @param additionalContent Optional additional metadata stored in the token
     * @return The created token
     * @throws ServiceException.UserNotFound If the user could not be found
     */
    suspend fun create(
        type: TwoFactorTokenType,
        userId: Long,
        expiringAt: Instant? = null,
        issuedAt: Instant = Clock.System.now(),
        additionalContent: String? = null
    ): TwoFactorToken

    /**
     * Marks a specific token as used.
     *
     * @param tokenId The id of the token to mark as used
     * @return The token for [tokenId]
     * @throws ServiceException.TwoFactorTokenNotFound If the token could not be found
     */
    suspend fun markAsUsed(tokenId: Long): TwoFactorToken

}